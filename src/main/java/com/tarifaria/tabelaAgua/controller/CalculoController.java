package com.tarifaria.tabelaAgua.controller;

import com.tarifaria.tabelaAgua.dto.CalculoRequest;
import com.tarifaria.tabelaAgua.dto.CalculoResponse;
import com.tarifaria.tabelaAgua.service.CalculoService;
import com.tarifaria.tabelaAgua.swagger.CalculoApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CalculoController implements CalculoApi {
    private final CalculoService calculoService;

    @Override
    public ResponseEntity<CalculoResponse> calcular(@RequestBody CalculoRequest req) {
        return ResponseEntity.ok(calculoService.calcular(req));
    }
}
