package com.pranav.toll.data;

import org.apache.commons.csv.CSVFormat;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class TollPlazaCatalog {

    // Keeps the bundled CSV data in memory for the lifetime of the application.
    private final List<TollPlaza> tollPlazas;

    public TollPlazaCatalog() {
        var resource = new ClassPathResource("data/toll_plaza_india_cleaned.csv");
        try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             var parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(reader)) {
            var loaded = new ArrayList<TollPlaza>();
            for (var row : parser) {
                try {
                    String name = row.get("toll_name").strip();
                    double latitude = Double.parseDouble(row.get("latitude"));
                    double longitude = Double.parseDouble(row.get("longitude"));
                    if (name.isBlank() || !Double.isFinite(latitude) || !Double.isFinite(longitude)
                            || Math.abs(latitude) > 90 || Math.abs(longitude) > 180) {
                        throw new IllegalArgumentException("Expected a toll name and valid coordinates");
                    }
                    loaded.add(new TollPlaza(name, latitude, longitude));
                } catch (IllegalArgumentException exception) {
                    throw new IllegalStateException("Invalid toll CSV record " + row.getRecordNumber()
                            + ": " + exception.getMessage(), exception);
                }
            }
            tollPlazas = List.copyOf(loaded);
        } catch (IOException | UncheckedIOException exception) {
            throw new IllegalStateException("Could not load data/toll_plaza_india_cleaned.csv", exception);
        }
    }

    public List<TollPlaza> getAll() {
        return tollPlazas;
    }
}
