package com.pranav.toll.service;

import com.pranav.toll.data.TollPlaza;
import com.pranav.toll.google.GoogleMapsClient.Coordinates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TollMatcher {

    private final double thresholdMetres;

    public TollMatcher(@Value("${toll.matching-threshold-metres:500}") double thresholdMetres) {
        this.thresholdMetres = thresholdMetres;
    }

    public List<Match> findMatches(List<TollPlaza> tollPlazas, List<Coordinates> routePoints) {
        var matches = new ArrayList<Match>();

        for (var tollPlaza : tollPlazas) {
            var closestDistance = Double.MAX_VALUE;
            var closestPointIndex = -1;

            for (var index = 0; index < routePoints.size(); index++) {
                var routePoint = routePoints.get(index);
                var distance = Haversine.distanceMeters(
                        tollPlaza.latitude(), tollPlaza.longitude(),
                        routePoint.latitude(), routePoint.longitude());

                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPointIndex = index;
                }
            }

            if (closestPointIndex >= 0 && closestDistance <= thresholdMetres) {
                matches.add(new Match(tollPlaza, closestPointIndex));
            }
        }

        return matches;
    }

    public record Match(TollPlaza tollPlaza, int closestRoutePointIndex) {
    }
}
