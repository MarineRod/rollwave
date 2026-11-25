package com.rollwave.component;


import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.rollwave.service.SkateparkService;

@Component
public class SkateparkImporter implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SkateparkImporter.class);
	private static final String CSV_FILE = "data/skateparks.csv";

	private final SkateparkService skateparkService;

	public SkateparkImporter(SkateparkService skateparkService) {
		this.skateparkService = skateparkService;
	}

	@Override
	public void run(String... args) throws Exception {
		ClassPathResource resource = new ClassPathResource(CSV_FILE);
		if (!resource.exists()) {
			log.warn("Fichier {} introuvable : aucun skatepark importé", CSV_FILE);
			return;
		}
		try (InputStream inputStream = resource.getInputStream()) {
			int count = skateparkService.importFromCsv(inputStream);
			log.info("{} skateparks importés depuis {}", count, CSV_FILE);
		}
	}
}