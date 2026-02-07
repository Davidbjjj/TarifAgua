package com.tarifaria.tabelaAgua.controller;

import com.tarifaria.tabelaAgua.dto.CalculoRequest;
import com.tarifaria.tabelaAgua.dto.CalculoResponse;
import com.tarifaria.tabelaAgua.service.CalculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calculos")
@RequiredArgsConstructor
public class CalculoController {
    private final CalculoService calculoService;

    @PostMapping
    public ResponseEntity<CalculoResponse> calcular(@RequestBody CalculoRequest req) {
        return ResponseEntity.ok(calculoService.calcular(req));
    }
}

