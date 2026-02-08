package com.tarifaria.tabelaAgua.controller;

import com.tarifaria.tabelaAgua.dto.TabelaRequest;
import com.tarifaria.tabelaAgua.model.TabelaTarifaria;
import com.tarifaria.tabelaAgua.service.TabelaService;
import com.tarifaria.tabelaAgua.swagger.TabelaApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TabelaController implements TabelaApi {
    private final TabelaService tabelaService;

    @Override
    public ResponseEntity<TabelaTarifaria> create(@Valid @RequestBody TabelaRequest req) {
        TabelaTarifaria created = tabelaService.createTabela(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    public ResponseEntity<List<TabelaTarifaria>> list() {
        return ResponseEntity.ok(tabelaService.listAll());
    }

    @Override
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tabelaService.deleteTabela(id);
        return ResponseEntity.noContent().build();
    }
}
