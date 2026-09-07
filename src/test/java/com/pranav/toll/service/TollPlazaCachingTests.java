package com.pranav.toll.service;

import com.pranav.toll.api.TollPlazaRequest;
import com.pranav.toll.data.TollPlazaCatalog;
import com.pranav.toll.google.GoogleMapsClient;
import com.pranav.toll.google.GoogleMapsClient.Coordinates;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class TollPlazaCachingTests {

    @Autowired
    private TollPlazaService tollPlazaService;

    @MockitoBean
    private GoogleMapsClient googleMapsClient;

    @MockitoBean
    private TollPlazaCatalog tollPlazaCatalog;

    @MockitoBean
    private TollMatcher tollMatcher;

    @Test
    void reusesSuccessfulEmptyResultWithoutRepeatingGoogleCalls() {
        var source = new Coordinates(12.0, 77.0);
        var destination = new Coordinates(12.02, 77.0);
        when(googleMapsClient.geocode("110001")).thenReturn(source);
        when(googleMapsClient.geocode("560001")).thenReturn(destination);
        when(googleMapsClient.route(source, destination))
                .thenReturn(new GoogleMapsClient.Route(25_000, List.of(source, destination)));
        when(tollPlazaCatalog.getAll()).thenReturn(List.of());
        when(tollMatcher.findMatches(anyList(), anyList())).thenReturn(List.of());

        var request = new TollPlazaRequest("110001", "560001");
        var first = tollPlazaService.findTollPlazas(request);
        var second = tollPlazaService.findTollPlazas(request);

        assertSame(first, second);
        assertTrue(second.tollPlazas().isEmpty());
        verify(googleMapsClient, times(1)).geocode("110001");
        verify(googleMapsClient, times(1)).geocode("560001");
        verify(googleMapsClient, times(1)).route(source, destination);
    }
}
