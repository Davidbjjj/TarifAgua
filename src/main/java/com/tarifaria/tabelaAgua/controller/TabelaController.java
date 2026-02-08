package com.tarifaria.tabelaAgua.controller;

import com.tarifaria.tabelaAgua.dto.TabelaRequest;
import com.tarifaria.tabelaAgua.model.TabelaTarifaria;
import com.tarifaria.tabelaAgua.service.TabelaService;
import com.tarifaria.tabelaAgua.swagger.TabelaApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TabelaController implements TabelaApi {
    private final TabelaService tabelaService;

    @Override
    public ResponseEntity<TabelaTarifaria> create(@RequestBody TabelaRequest req) {
        return ResponseEntity.ok(tabelaService.createTabela(req));
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
