package com.pranav.toll.service;

import com.pranav.toll.data.TollPlaza;
import com.pranav.toll.google.GoogleMapsClient.Coordinates;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TollMatcherTests {

    @Test
    void matchesOnlyTollsWithinFiveHundredMetres() {
        var matcher = new TollMatcher(500);
        var inside = new TollPlaza("Inside", 12.001, 77.0);
        var outside = new TollPlaza("Outside", 12.01, 77.0);

        var matches = matcher.findMatches(
                List.of(inside, outside),
                List.of(new Coordinates(12.0, 77.0)));

        assertEquals(List.of(new TollMatcher.Match(inside, 0)), matches);
    }
}
