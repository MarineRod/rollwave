package com.rollwave.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rollwave.dto.SkateparkDto;
import com.rollwave.service.SkateparkService;

@RestController
@RequestMapping("/skateparks")
public class SkateparkController {

	private final SkateparkService skateparkService;

	public SkateparkController(SkateparkService skateparkService) {
		this.skateparkService = skateparkService;
	}

	@GetMapping("/nearby")
	public List<SkateparkDto> nearby(@RequestParam double lat, @RequestParam double lng,
			@RequestParam(defaultValue = "5000") double radius) {
		return skateparkService.findNearby(lat, lng, radius);
	}

	/**
	 * Recherche par texte libre : adresse complète ou partielle, ou nom de ville.
	 * Exemples : GET /skateparks/search?q=Toulouse GET /skateparks/search?q=allée
	 * des Argoulets Toulouse GET /skateparks/search?q=12 rue du Port 29900
	 * Concarneau&radius=3000
	 */
	@GetMapping("/search")
	public List<SkateparkDto> search(@RequestParam String q, @RequestParam(defaultValue = "2000") double radius) {
		return skateparkService.search(q, radius);
	}

}