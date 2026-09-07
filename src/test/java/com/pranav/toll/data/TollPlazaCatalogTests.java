package com.pranav.toll.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TollPlazaCatalogTests {

    @Test
    void loadsBundledCsvWithoutSwappingLatitudeAndLongitude() {
        var tolls = new TollPlazaCatalog().getAll();

        assertEquals(1536, tolls.size());
        assertEquals(new TollPlaza("Dhamnod Toll Plaza", 23.41805978, 74.99684426), tolls.getFirst());
    }
}
