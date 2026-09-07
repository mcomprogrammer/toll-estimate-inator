package com.pranav.toll.service;

import com.pranav.toll.api.TollPlazaRequest;
import com.pranav.toll.data.TollPlaza;
import com.pranav.toll.data.TollPlazaCatalog;
import com.pranav.toll.google.GoogleMapsClient;
import com.pranav.toll.google.GoogleMapsClient.Coordinates;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TollPlazaServiceTests {

    @Test
    void buildsSortedTollsUsingCumulativeRouteDistance() {
        var google = mock(GoogleMapsClient.class);
        var catalog = mock(TollPlazaCatalog.class);
        var service = new TollPlazaService(google, catalog, new TollMatcher(500));
        var source = new Coordinates(12.0, 77.0);
        var destination = new Coordinates(12.02, 77.0);
        var routePoints = List.of(source, new Coordinates(12.0, 77.01), destination);

        when(google.geocode("110001")).thenReturn(source);
        when(google.geocode("560001")).thenReturn(destination);
        when(google.route(source, destination)).thenReturn(new GoogleMapsClient.Route(25_000, routePoints));
        var laterToll = new TollPlaza("Later", 12.0205, 77.0);
        var earlierToll = new TollPlaza("Earlier", 12.0, 77.0105);
        when(catalog.getAll()).thenReturn(List.of(laterToll, earlierToll));

        var response = service.findTollPlazas(new TollPlazaRequest("110001", "560001"));

        assertEquals(25.0, response.route().distanceInKm());
        assertEquals(List.of("Earlier", "Later"),
                response.tollPlazas().stream().map(com.pranav.toll.api.TollPlazaResponse.TollPlaza::name).toList());
        var firstSegmentKm = Haversine.distanceMeters(
                source.latitude(), source.longitude(),
                routePoints.get(1).latitude(), routePoints.get(1).longitude()) / 1000.0;
        var secondSegmentKm = Haversine.distanceMeters(
                routePoints.get(1).latitude(), routePoints.get(1).longitude(),
                destination.latitude(), destination.longitude()) / 1000.0;
        assertEquals(firstSegmentKm, response.tollPlazas().get(0).distanceFromSource(), 0.001);
        assertEquals(firstSegmentKm + secondSegmentKm,
                response.tollPlazas().get(1).distanceFromSource(), 0.001);
        assertTrue(response.tollPlazas().get(0).distanceFromSource()
                < response.tollPlazas().get(1).distanceFromSource());
    }

    @Test
    void keepsRouteWhenNoTollsMatch() {
        var google = mock(GoogleMapsClient.class);
        var catalog = mock(TollPlazaCatalog.class);
        var service = new TollPlazaService(google, catalog, new TollMatcher(500));
        var source = new Coordinates(12.0, 77.0);
        var destination = new Coordinates(12.02, 77.0);
        when(google.geocode("110001")).thenReturn(source);
        when(google.geocode("560001")).thenReturn(destination);
        when(google.route(source, destination)).thenReturn(
                new GoogleMapsClient.Route(25_000, List.of(source, destination)));
        when(catalog.getAll()).thenReturn(List.of(new TollPlaza("Far", 20.0, 77.0)));

        var response = service.findTollPlazas(new TollPlazaRequest("110001", "560001"));

        assertEquals(25.0, response.route().distanceInKm());
        assertTrue(response.tollPlazas().isEmpty());
    }
}
