package com.pranav.toll.service;

import com.pranav.toll.api.TollPlazaRequest;
import com.pranav.toll.api.TollPlazaResponse;
import com.pranav.toll.data.TollPlazaCatalog;
import com.pranav.toll.exception.SamePincodeException;
import com.pranav.toll.google.GoogleMapsClient;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class TollPlazaService {

    private final GoogleMapsClient googleMapsClient;
    private final TollPlazaCatalog tollPlazaCatalog;
    private final TollMatcher tollMatcher;

    public TollPlazaService(GoogleMapsClient googleMapsClient,
                            TollPlazaCatalog tollPlazaCatalog,
                            TollMatcher tollMatcher) {
        this.googleMapsClient = googleMapsClient;
        this.tollPlazaCatalog = tollPlazaCatalog;
        this.tollMatcher = tollMatcher;
    }

    public TollPlazaResponse findTollPlazas(TollPlazaRequest request) {
        if (request != null && java.util.Objects.equals(request.sourcePincode(), request.destinationPincode())) {
            throw new SamePincodeException();
        }

        var source = googleMapsClient.geocode(request.sourcePincode());
        var destination = googleMapsClient.geocode(request.destinationPincode());
        var route = googleMapsClient.route(source, destination);
        var cumulativeDistances = cumulativeDistances(route.points());
        var matches = tollMatcher.findMatches(tollPlazaCatalog.getAll(), route.points());

        var tollPlazas = matches.stream()
                .map(match -> {
                    var tollPlaza = match.tollPlaza();
                    return new TollPlazaResponse.TollPlaza(
                            tollPlaza.name(),
                            tollPlaza.latitude(),
                            tollPlaza.longitude(),
                            cumulativeDistances[match.closestRoutePointIndex()] / 1000.0);
                })
                .sorted(Comparator.comparingDouble(TollPlazaResponse.TollPlaza::distanceFromSource))
                .toList();

        return new TollPlazaResponse(
                new TollPlazaResponse.Route(
                        request.sourcePincode(),
                        request.destinationPincode(),
                        route.distanceMeters() / 1000.0),
                tollPlazas);
    }

    private static double[] cumulativeDistances(List<GoogleMapsClient.Coordinates> routePoints) {
        var cumulativeDistances = new double[routePoints.size()];
        for (var index = 1; index < routePoints.size(); index++) {
            var previous = routePoints.get(index - 1);
            var current = routePoints.get(index);
            cumulativeDistances[index] = cumulativeDistances[index - 1]
                    + Haversine.distanceMeters(
                    previous.latitude(), previous.longitude(),
                    current.latitude(), current.longitude());
        }
        return cumulativeDistances;
    }
}
