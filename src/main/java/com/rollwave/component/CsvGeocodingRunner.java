package com.rollwave.component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.rollwave.dto.GeocodingDto.Properties;

/**
 * Outil à lancer UNE SEULE FOIS, avec le profil Spring "geocode" : lit
 * data/skateparks.csv, retrouve l'adresse de chaque équipement par géocodage
 * inverse, et écrit un nouveau fichier CSV avec 4 colonnes en plus : adresse,
 * code_postal, ville, code_insee.
 *
 * Il ne s'exécute jamais en fonctionnement normal (ni en local, ni sur Render,
 * ni dans les tests).
 */
@Component
@Profile("geocode")
public class CsvGeocodingRunner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(CsvGeocodingRunner.class);

	private static final String INPUT_FILE = "data/skateparks.csv";
	private static final List<String> NEW_COLUMNS = List.of("adresse", "code_postal", "ville", "code_insee");

	/**
	 * Pause entre deux appels : environ 30 requêtes par seconde, sous la limite de
	 * 50.
	 */
	private static final long PAUSE_MS = 30;

	/**
	 * Au-delà de cette distance, l'adresse trouvée est trop loin pour être celle du
	 * skatepark.
	 */
	private static final double MAX_STREET_DISTANCE_METERS = 300;

	private final GeocodingClient geocodingClient;
	private final String outputFile;

	public CsvGeocodingRunner(GeocodingClient geocodingClient,
			@Value("${geocoding.output-file:skateparks-geocoded.csv}") String outputFile) {
		this.geocodingClient = geocodingClient;
		this.outputFile = outputFile;
	}

	@Override
	public void run(String... args) throws Exception {
		CSVFormat inputFormat = CSVFormat.DEFAULT.builder().setDelimiter(';').setHeader().setSkipHeaderRecord(true)
				.setIgnoreEmptyLines(true).get();
		CSVFormat outputFormat = CSVFormat.DEFAULT.builder().setDelimiter(';').get();

		Path output = Path.of(outputFile).toAbsolutePath();
		log.info("Géocodage inverse de {} vers {}", INPUT_FILE, output);

		int total = 0;
		int found = 0;
		try (Reader reader = new InputStreamReader(new ClassPathResource(INPUT_FILE).getInputStream(),
				StandardCharsets.UTF_8);
				CSVParser parser = CSVParser.parse(reader, inputFormat);
				Writer writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8);
				CSVPrinter printer = new CSVPrinter(writer, outputFormat)) {

			// Excel ajoute parfois un caractère invisible (BOM) devant le premier nom de
			// colonne
			List<String> header = parser.getHeaderNames().stream().map(name -> name.replace("﻿", "")).toList();
			List<String> newHeader = new ArrayList<>(header);
			newHeader.addAll(NEW_COLUMNS);
			printer.printRecord(newHeader);

			for (CSVRecord row : parser) {
				total++;
				List<String> values = new ArrayList<>(row.toList());
				while (values.size() < header.size()) {
					values.add("");
				}

				List<String> address = geocode(row);
				if (!address.get(2).isBlank()) {
					found++;
				}
				values.addAll(address);
				printer.printRecord(values);

				if (total % 100 == 0) {
					log.info("{} lignes traitées ({} adresses trouvées)", total, found);
				}
			}
		}

		logResult(output, total, found);
	}

	/**
	 * Un appel, puis un second après une pause d'une seconde si l'API signale trop
	 * de requêtes.
	 */
	private Optional<Properties> callWithRetry(double latitude, double longitude) throws InterruptedException {
		try {
			return geocodingClient.reverse(latitude, longitude);
		} catch (HttpClientErrorException.TooManyRequests e) {
			Thread.sleep(1000);
		} catch (RestClientException e) {
			log.warn("Échec du géocodage pour ({}, {}) : {}", latitude, longitude, e.getMessage());
			return Optional.empty();
		}

		// Second essai après la pause
		try {
			return geocodingClient.reverse(latitude, longitude);
		} catch (RestClientException e) {
			log.warn("Échec du géocodage pour ({}, {}) : {}", latitude, longitude, e.getMessage());
			return Optional.empty();
		}
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

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	// =============================
	// MÉTHODES PRIVÉES
	// =============================


	/**
	 * Renvoie [adresse, code_postal, ville, code_insee], avec des valeurs vides si
	 * rien n'est trouvé.
	 */
	private List<String> geocode(CSVRecord row) throws InterruptedException {
		Double latitude = parseDouble(row.get("coordonnees_y"));
		Double longitude = parseDouble(row.get("coordonnees_x"));
		if (latitude == null || longitude == null) {
			return List.of("", "", "", "");
		}

		Optional<Properties> result = callWithRetry(latitude, longitude);
		Thread.sleep(PAUSE_MS);

		if (result.isEmpty()) {
			return List.of("", "", "", "");
		}

		Properties place = result.get();
		String postcode = nullToEmpty(place.postcode());
		String city = nullToEmpty(place.city());

		// Adresse trop éloignée (skatepark au milieu d'un parc, par exemple) : on garde
		// seulement la ville
		boolean tooFar = place.distance() != null && place.distance() > MAX_STREET_DISTANCE_METERS;
		String address = tooFar ? (postcode + " " + city).trim() : nullToEmpty(place.label());

		return List.of(address, postcode, city, nullToEmpty(place.citycode()));
	}

	
	
	/**
	 * Vérifie que le fichier a bien été écrit, et affiche un récapitulatif dans la
	 * console.
	 */
	private void logResult(Path output, int total, int found) throws IOException {
		if (!Files.exists(output)) {
			log.error("ÉCHEC : le fichier enrichi n'a pas été créé ({})", output);
			return;
		}

		long sizeKb = Files.size(output) / 1024;
		int missing = total - found;

		log.info("==============================================================");
		log.info(" Fichier CSV enrichi créé avec succès");
		log.info("  Emplacement        : {}", output);
		log.info("  Taille             : {} Ko", sizeKb);
		log.info("  Lignes traitées    : {}", total);
		log.info("  Adresses trouvées  : {}", found);
		log.info("  Sans adresse       : {}", missing);
		log.info(" Étape suivante : remplacez src/main/resources/data/skateparks.csv");
		log.info(" par ce fichier, puis arrêtez l'application.");
		log.info("==============================================================");
	}
}
