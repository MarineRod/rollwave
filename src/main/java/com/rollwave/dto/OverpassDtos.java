package com.rollwave.dto;

import java.util.List;
import java.util.Map;

public final class OverpassDtos {

	private OverpassDtos() {
	}

	public record OverpassResponse(List<OverpassElement> elements) {
	}

	public record OverpassElement(String type, long id, Double lat, Double lon, Center center,
			Map<String, String> tags) {

		public double latitude() {
			return lat != null ? lat : center.lat();
		}

		public double longitude() {
			return lon != null ? lon : center.lon();
		}

		public String tag(String key) {
			return tags == null ? null : tags.get(key);
		}
	}

	public record Center(double lat, double lon) {
	}
}