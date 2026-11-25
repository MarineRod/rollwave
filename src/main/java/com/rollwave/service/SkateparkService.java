package com.rollwave.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.rollwave.component.OverpassClient;
import com.rollwave.dao.SkateparkDao;
import com.rollwave.dto.OverpassDtos.OverpassElement;
import com.rollwave.dto.SkateparkDto;
import com.rollwave.entity.Skatepark;

@Service
public class SkateparkService {

	private static final double MIN_RADIUS_METERS = 100;
	private static final double MAX_RADIUS_METERS = 20_000;

	private final ModelMapper modelMapper;
	private final SkateparkDao skateparkDao;
	private final OverpassClient overpassClient;

	public SkateparkService(SkateparkDao skateparkDao, ModelMapper modelMapper, OverpassClient overpassClient) {
		this.skateparkDao = skateparkDao;
		this.modelMapper = modelMapper;
		this.overpassClient = overpassClient;
	}

	/**
	     * Récupère les skateparks autour d'une position depuis OpenStreetMap,
	     * les enregistre ou les met à jour en base, puis les renvoie.
	     * Si Overpass ne répond pas, renvoie une erreur 503.
	     */
	    @Transactional
	    public List<SkateparkDto> findNearby(double latitude, double longitude, double radiusMeters) {
	        double radius = Math.clamp(radiusMeters, MIN_RADIUS_METERS, MAX_RADIUS_METERS);

	        List<OverpassElement> elements;
	        try {
	            elements = overpassClient.findSkateparks(latitude, longitude, radius);
	        } catch (RestClientException e) {
	            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
	                    "Le service de cartographie est momentanément indisponible, réessayez plus tard");
	        }

	        return elements.stream()
	                .map(this::saveOrUpdate)
	                .map(skatepark -> modelMapper.map(skatepark, SkateparkDto.class))
	                .toList();
	    }

	// ---------- Méthodes privées ----------

	private Skatepark saveOrUpdate(OverpassElement element) {
		String externalId = "osm:" + element.type() + "/" + element.id();
		Skatepark skatepark = skateparkDao.findByExternalId(externalId).orElseGet(Skatepark::new);

		String name = element.tag("name");
		skatepark.setExternalId(externalId);
		skatepark.setName(name != null ? name : "Skatepark");
		skatepark.setAdress(buildAddress(element));
		skatepark.setLatitude(element.latitude());
		skatepark.setLongitude(element.longitude());
		// user reste null : spot importé, retrouvé par findByUserIsNull()

		return skateparkDao.save(skatepark);
	}

	private String buildAddress(OverpassElement element) {
		String address = Stream.of(element.tag("addr:housenumber"), element.tag("addr:street"),
				element.tag("addr:postcode"), element.tag("addr:city")).filter(Objects::nonNull)
				.collect(Collectors.joining(" "));

		return address.isBlank() ? "Adresse non renseignée" : address;
	}
}