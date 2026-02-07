package com.tarifaria.tabelaAgua.controller;

import com.tarifaria.tabelaAgua.dto.TabelaRequest;
import com.tarifaria.tabelaAgua.model.TabelaTarifaria;
import com.tarifaria.tabelaAgua.service.TabelaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tabelas-tarifarias")
@RequiredArgsConstructor
public class TabelaController {
    private final TabelaService tabelaService;

    @PostMapping
    public ResponseEntity<TabelaTarifaria> create(@RequestBody TabelaRequest req) {
        return ResponseEntity.ok(tabelaService.createTabela(req));
    }

    @GetMapping
    public ResponseEntity<List<TabelaTarifaria>> list() {
        return ResponseEntity.ok(tabelaService.listAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tabelaService.deleteTabela(id);
        return ResponseEntity.noContent().build();
    }
}

