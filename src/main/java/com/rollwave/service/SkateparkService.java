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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rollwave.dao.SkateparkDao;
import com.rollwave.dto.SkateparkDto;
import com.rollwave.entity.Skatepark;
import com.rollwave.enums.SkateparkType;

@Service
public class SkateparkService {

    private static final Logger log = LoggerFactory.getLogger(SkateparkService.class);

    private static final double MIN_RADIUS_METERS = 100;
    private static final double MAX_RADIUS_METERS = 20_000;
    private static final double METERS_PER_DEGREE = 111_320;

    private final SkateparkDao skateparkDao;
    private final ModelMapper modelMapper;

    public SkateparkService(SkateparkDao skateparkDao, ModelMapper modelMapper) {
        this.skateparkDao = skateparkDao;
        this.modelMapper = modelMapper;
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

        return skateparkDao.findByLatitudeBetweenAndLongitudeBetween(
                        latitude - deltaLat, latitude + deltaLat,
                        longitude - deltaLng, longitude + deltaLng)
                .stream()
                .map(skatepark -> modelMapper.map(skatepark, SkateparkDto.class))
                .toList();
    }

    // ---------- Import du fichier Data ES ----------

    /**
     * Lit le fichier CSV Data ES et enregistre ou met à jour chaque équipement.
     * Renvoie le nombre de lignes enregistrées.
     */
    @Transactional
    public int importFromCsv(InputStream inputStream) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setHeader()                 // la première ligne donne le nom des colonnes
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
        skatepark.setExternalId(externalId);
        skatepark.setName(name.isBlank() ? type.get().getLabel() : name);
        skatepark.setType(type.get());
        skatepark.setAdress("Adresse non renseignée");
        skatepark.setLatitude(latitude);
        skatepark.setLongitude(longitude);
        // user reste null : spot officiel, retrouvé par findByUserIsNull()

        skateparkDao.save(skatepark);
        return true;
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
}