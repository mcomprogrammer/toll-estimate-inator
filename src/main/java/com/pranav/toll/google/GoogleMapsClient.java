package com.pranav.toll.google;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GoogleMapsClient {

    // Converts an Indian pincode into the coordinate returned by Google Geocoding.

    private final RestClient client;
    private final RestClient routesClient;
    private final String apiKey;

    public GoogleMapsClient(@Value("${google.maps.api-key:}") String apiKey) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3_000);
        factory.setReadTimeout(5_000);
        this.client = RestClient.builder().baseUrl("https://maps.googleapis.com")
                .requestFactory(factory).build();
        this.routesClient = RestClient.builder().baseUrl("https://routes.googleapis.com")
                .requestFactory(factory).build();
        this.apiKey = apiKey;
    }

    public Coordinates geocode(String pincode) {
        if (apiKey.isBlank()) {
            throw new GoogleMapsException("Set GOOGLE_MAPS_API_KEY in the application environment");
        }
        final JsonNode body;
        try {
            body = client.get().uri(builder -> builder.path("/maps/api/geocode/json")
                            .queryParam("components", "postal_code:" + pincode + "|country:IN")
                            .queryParam("key", apiKey).build())
                    .retrieve().body(JsonNode.class);
        } catch (RestClientException exception) {
            // HTTP exceptions can contain the API key in their URL; do not propagate them.
            throw new GoogleMapsException("Google geocoding request failed");
        }
        var results = body == null ? null : body.path("results");
        if (results == null || results.isEmpty()) {
            throw new GoogleMapsException("Google could not resolve the pincode");
        }
        var location = results.get(0).path("geometry").path("location");
        return new Coordinates(location.path("lat").asDouble(), location.path("lng").asDouble());
    }

    public Route route(Coordinates origin, Coordinates destination) {
        if (apiKey.isBlank()) {
            throw new GoogleMapsException("Set GOOGLE_MAPS_API_KEY in the application environment");
        }
        final JsonNode body;
        try {
            body = routesClient.post().uri("/directions/v2:computeRoutes")
                    .header("Content-Type", "application/json")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "routes.distanceMeters,routes.polyline.geoJsonLinestring")
                    .body(Map.of(
                            "origin", waypoint(origin),
                            "destination", waypoint(destination),
                            "travelMode", "DRIVE",
                            "routingPreference", "TRAFFIC_UNAWARE",
                            "computeAlternativeRoutes", false,
                            "polylineQuality", "HIGH_QUALITY",
                            "polylineEncoding", "GEO_JSON_LINESTRING"))
                    .retrieve().body(JsonNode.class);
        } catch (RestClientException exception) {
            throw new GoogleMapsException("Google route request failed");
        }

        var routes = body == null ? null : body.path("routes");
        if (routes == null || routes.isEmpty()) {
            throw new GoogleMapsException("Google could not find a driving route");
        }
        var route = routes.get(0);
        var coordinates = route.path("polyline").path("geoJsonLinestring").path("coordinates");

        var points = new ArrayList<Coordinates>();
        for (var coordinate : coordinates) {
            points.add(new Coordinates(coordinate.get(1).asDouble(), coordinate.get(0).asDouble()));
        }
        return new Route(route.path("distanceMeters").asDouble(), points);
    }

    private static Map<String, Object> waypoint(Coordinates coordinates) {
        return Map.of("location", Map.of("latLng", Map.of(
                "latitude", coordinates.latitude(),
                "longitude", coordinates.longitude())));
    }

    public record Coordinates(double latitude, double longitude) {
    }

    public record Route(double distanceMeters, List<Coordinates> points) {
        public Route {
            points = List.copyOf(points);
        }
    }
}
