package com.pranav.toll.service;

public final class Haversine {

    private static final double EARTH_RADIUS_METRES = 6_371_000;

    private Haversine() {
    }

    public static double distanceMeters(double latitudeA, double longitudeA,
                                        double latitudeB, double longitudeB) {
        var latitudeDelta = Math.toRadians(latitudeB - latitudeA);
        var longitudeDelta = Math.toRadians(longitudeB - longitudeA);
        var latitudeARadians = Math.toRadians(latitudeA);
        var latitudeBRadians = Math.toRadians(latitudeB);

        var haversine = Math.pow(Math.sin(latitudeDelta / 2), 2)
                + Math.cos(latitudeARadians) * Math.cos(latitudeBRadians)
                * Math.pow(Math.sin(longitudeDelta / 2), 2);

        return 2 * EARTH_RADIUS_METRES
                * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }
}
