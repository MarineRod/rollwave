package com.rollwave.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.rollwave.component.GeocodingClient;
import com.rollwave.dao.SkateparkDao;
import com.rollwave.dto.GeocodingDto.Feature;
import com.rollwave.dto.SkateparkDto;
import com.rollwave.entity.Skatepark;
import com.rollwave.enums.SkateparkType;

@Service
public class SkateparkService {

    private static final Logger log = LoggerFactory.getLogger(SkateparkService.class);

    private static final double MIN_RADIUS_METERS = 100;
    private static final double MAX_RADIUS_METERS = 20_000;
    private static final double METERS_PER_DEGREE = 111_320;
    private static final int MIN_QUERY_LENGTH = 3;
    private static final String UNKNOWN_ADDRESS = "Adresse non renseignée";

    private final SkateparkDao skateparkDao;
    private final ModelMapper modelMapper;
    private final GeocodingClient geocodingClient;

    public SkateparkService(SkateparkDao skateparkDao,
                            ModelMapper modelMapper,
                            GeocodingClient geocodingClient) {
        this.skateparkDao = skateparkDao;
        this.modelMapper = modelMapper;
        this.geocodingClient = geocodingClient;
    }

    // ---------- Recherche autour d'une position ----------

    /**
     * Renvoie les skateparks situés dans un carré centré sur la position,
     * de côté égal à deux fois le rayon.
     */
    @Transactional(readOnly = true)
    public List<SkateparkDto> findNearby(double latitude, double longitude, double radiusMeters) {
        double radius = Math.clamp(radiusMeters, MIN_RADIUS_METERS, MAX_RADIUS_METERS);
        double deltaLat = radius / METERS_PER_DEGREE;
        double deltaLng = radius / (METERS_PER_DEGREE * Math.cos(Math.toRadians(latitude)));

        return toDtos(skateparkDao.findByLatitudeBetweenAndLongitudeBetween(
                latitude - deltaLat, latitude + deltaLat,
                longitude - deltaLng, longitude + deltaLng));
    }

    // ---------- Recherche par adresse ou par ville ----------

    /**
     * Recherche à partir d'un texte libre : une adresse complète ("12 rue du Port Toulouse"),
     * partielle ("rue du Port Toulouse") ou un nom de ville ("Toulouse").
     *
     * - Si le texte désigne une commune : tous les skateparks de cette commune.
     * - Si le texte désigne une adresse ou une rue : les skateparks autour de ce point.
     * - Si le service de géocodage ne trouve rien ou ne répond pas : recherche du texte dans le nom des villes.
     */
    @Transactional(readOnly = true)
    public List<SkateparkDto> search(String query, double radiusMeters) {
        String text = query == null ? "" : query.trim();
        if (text.length() < MIN_QUERY_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La recherche doit contenir au moins " + MIN_QUERY_LENGTH + " caractères");
        }

        Optional<Feature> place;
        try {
            place = geocodingClient.search(text);
        } catch (RestClientException e) {
            log.warn("Service de géocodage indisponible, recherche sur le nom de ville : {}", e.getMessage());
            place = Optional.empty();
        }

        if (place.isEmpty() || place.get().properties() == null) {
            return toDtos(skateparkDao.findByCityContainingIgnoreCase(text));
        }

        Feature feature = place.get();
        if ("municipality".equals(feature.properties().type())) {
            return findByCity(feature);
        }

        Double latitude = feature.geometry() == null ? null : feature.geometry().latitude();
        Double longitude = feature.geometry() == null ? null : feature.geometry().longitude();
        if (latitude == null || longitude == null) {
            return toDtos(skateparkDao.findByCityContainingIgnoreCase(text));
        }
        return findNearby(latitude, longitude, radiusMeters);
    }

    /**
     * Tous les skateparks d'une commune : d'abord par code INSEE, sinon par nom.
     * Le nom sert notamment pour Paris, Lyon et Marseille, dont les skateparks
     * portent le code INSEE de leur arrondissement et non celui de la ville.
     */
    private List<SkateparkDto> findByCity(Feature municipality) {
        String cityCode = municipality.properties().citycode();
        if (cityCode != null) {
            List<Skatepark> byCode = skateparkDao.findByCityCode(cityCode);
            if (!byCode.isEmpty()) {
                return toDtos(byCode);
            }
        }
        String city = municipality.properties().city();
        return city == null ? List.of() : toDtos(skateparkDao.findByCityIgnoreCase(city));
    }

    // ---------- Import du fichier Data ES ----------

    /**
     * Lit le fichier CSV Data ES et enregistre ou met à jour chaque équipement.
     * Les colonnes adresse, code_postal, ville et code_insee sont lues si elles existent
     * (fichier enrichi par l'outil de géocodage), et ignorées sinon.
     * Renvoie le nombre de lignes enregistrées.
     */
    @Transactional
    public int importFromCsv(InputStream inputStream) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .get();

        int imported = 0;
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVParser.parse(reader, format)) {

            for (CSVRecord row : parser) {
                Double[] position = readPosition(row);
                if (position == null) {
                    log.warn("Ligne {} ignorée : coordonnées absentes", row.getRecordNumber());
                    continue;
                }
                if (saveOrUpdate(row, position[0], position[1])) {
                    imported++;
                }
            }
        }
        return imported;
    }

    // ---------- Méthodes privées ----------

    private boolean saveOrUpdate(CSVRecord row, double latitude, double longitude) {
        Optional<SkateparkType> type = SkateparkType.fromLabel(row.get("type"));
        if (type.isEmpty()) {
            log.warn("Ligne {} ignorée : type inconnu « {} »", row.getRecordNumber(), row.get("type"));
            return false;
        }

        String externalId = "dataes:" + row.get("numero");
        Skatepark skatepark = skateparkDao.findByExternalId(externalId)
                .orElseGet(Skatepark::new);

        String name = row.get("nom");
        String address = optionalColumn(row, "adresse");

        skatepark.setExternalId(externalId);
        skatepark.setName(name.isBlank() ? type.get().getLabel() : name);
        skatepark.setType(type.get());
        skatepark.setAdress(address.isBlank() ? UNKNOWN_ADDRESS : address);
        skatepark.setPostcode(blankToNull(optionalColumn(row, "code_postal")));
        skatepark.setCity(blankToNull(optionalColumn(row, "ville")));
        skatepark.setCityCode(blankToNull(optionalColumn(row, "code_insee")));
        skatepark.setLatitude(latitude);
        skatepark.setLongitude(longitude);
        // user reste null : spot officiel, retrouvé par findByUserIsNull()

        skateparkDao.save(skatepark);
        return true;
    }

    /** Valeur d'une colonne facultative : chaîne vide si la colonne n'existe pas dans le fichier. */
    private String optionalColumn(CSVRecord row, String column) {
        return row.isMapped(column) ? row.get(column) : "";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    /** Renvoie {latitude, longitude}, ou null si la ligne n'a pas de coordonnées exploitables. */
    private Double[] readPosition(CSVRecord row) {
        Double latitude = parseDouble(row.get("coordonnees_y"));
        Double longitude = parseDouble(row.get("coordonnees_x"));

        if (latitude == null || longitude == null) {
            // Solution de secours : la colonne "coordonnees" au format "43.622811, 1.47956"
            String[] parts = row.get("coordonnees").split(",");
            if (parts.length == 2) {
                latitude = parseDouble(parts[0]);
                longitude = parseDouble(parts[1]);
            }
        }
        return (latitude == null || longitude == null) ? null : new Double[] { latitude, longitude };
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<SkateparkDto> toDtos(List<Skatepark> skateparks) {
        return skateparks.stream()
                .map(skatepark -> modelMapper.map(skatepark, SkateparkDto.class))
                .toList();
    }
}
