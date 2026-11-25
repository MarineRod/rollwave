package com.rollwave.component;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.rollwave.dto.OverpassDtos.OverpassElement;
import com.rollwave.dto.OverpassDtos.OverpassResponse;

@Component
public class OverpassClient {

	private static final String QUERY = """
			[out:json][timeout:25];
			(
			  nwr["sport"="skateboard"](around:%.0f,%.6f,%.6f);
			  nwr["leisure"="skatepark"](around:%.0f,%.6f,%.6f);
			);
			out center tags;
			""";

	private final RestClient restClient;

	public OverpassClient(@Value("${overpass.url}") String url, @Value("${overpass.user-agent}") String userAgent) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(5));
		requestFactory.setReadTimeout(Duration.ofSeconds(30));

		this.restClient = RestClient.builder().baseUrl(url).requestFactory(requestFactory)
				.defaultHeader("User-Agent", userAgent).build();
	}

	public List<OverpassElement> findSkateparks(double latitude, double longitude, double radiusMeters) {
		// Locale.ROOT : points décimaux (48.85), jamais de virgules, même sur un PC en
		// français
		String query = String.format(Locale.ROOT, QUERY, radiusMeters, latitude, longitude, radiusMeters, latitude,
				longitude);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("data", query);

		OverpassResponse response = restClient.post().contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
				.retrieve().body(OverpassResponse.class);

		return (response == null || response.elements() == null) ? List.of() : response.elements();
	}
}