package com.pranav.toll.api;

import java.util.List;

public record TollPlazaResponse(Route route, List<TollPlaza> tollPlazas) {

    public record Route(String sourcePincode, String destinationPincode, double distanceInKm) {
    }

    public record TollPlaza(String name, double latitude, double longitude, double distanceFromSource) {
    }
}
