package com.rollwave.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Réponses de l'API de géocodage de la Géoplateforme (IGN), au format GeoJSON.
 * Seuls les champs utiles à Rollwave sont déclarés : les autres sont ignorés.
 */
public final class GeocodingDto {

	private GeocodingDto() {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record GeocodingResponse(List<Feature> features) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Feature(Geometry geometry, Properties properties) {
	}

	/** En GeoJSON, les coordonnées sont dans l'ordre [longitude, latitude]. */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Geometry(List<Double> coordinates) {

		public Double longitude() {
			return coordinates == null || coordinates.size() < 2 ? null : coordinates.get(0);
		}

		public Double latitude() {
			return coordinates == null || coordinates.size() < 2 ? null : coordinates.get(1);
		}
	}

	/**
	 * label : adresse complète, par exemple "12 Rue du Port 31000 Toulouse" type :
	 * "housenumber", "street", "locality" ou "municipality" citycode : code INSEE
	 * de la commune distance : distance en mètres entre le point demandé et
	 * l'adresse trouvée (géocodage inverse)
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Properties(String label, String type, String name, String postcode, String city, String citycode,
			Double score, Double distance) {
	}
}
