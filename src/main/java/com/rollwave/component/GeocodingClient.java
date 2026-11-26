package com.rollwave.component;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.rollwave.dto.GeocodingDto.Feature;
import com.rollwave.dto.GeocodingDto.GeocodingResponse;
import com.rollwave.dto.GeocodingDto.Properties;

/**
 * Client de l'API de géocodage de la Géoplateforme (IGN), gratuite et sans clé.
 * Limite d'usage : 50 requêtes par seconde depuis une même adresse IP.
 */
@Component
public class GeocodingClient {

    private final RestClient restClient;

    public GeocodingClient(@Value("${geocoding.url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /** Géocodage inverse : l'adresse la plus proche d'une position. */
    public Optional<Properties> reverse(double latitude, double longitude) {
        GeocodingResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/reverse")
                        .queryParam("lat", latitude)
                        .queryParam("lon", longitude)
                        .queryParam("index", "address")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .body(GeocodingResponse.class);

        return firstFeature(response).map(Feature::properties);
    }

    /** Géocodage : le lieu (adresse, rue ou commune) qui correspond le mieux au texte saisi. */
    public Optional<Feature> search(String query) {
        GeocodingResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search")
                        .queryParam("q", query)
                        .queryParam("index", "address")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .body(GeocodingResponse.class);

        return firstFeature(response);
    }

    private Optional<Feature> firstFeature(GeocodingResponse response) {
        if (response == null || response.features() == null || response.features().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(response.features().get(0));
    }
}
