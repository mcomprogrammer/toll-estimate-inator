package com.pranav.toll.api;

import com.pranav.toll.service.TollPlazaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/toll-plazas")
public class TollPlazaController {

    private final TollPlazaService tollPlazaService;

    public TollPlazaController(TollPlazaService tollPlazaService) {
        this.tollPlazaService = tollPlazaService;
    }

    @PostMapping
    public TollPlazaResponse findTollPlazas(@Valid @RequestBody TollPlazaRequest request) {
        return tollPlazaService.findTollPlazas(request);
    }
}
