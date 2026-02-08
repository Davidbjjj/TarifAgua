package com.tarifaria.tabelaAgua.swagger;

// ...existing code...
import com.tarifaria.tabelaAgua.dto.TabelaRequest;
import com.tarifaria.tabelaAgua.model.TabelaTarifaria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tabela Tarifaria", description = "CRUD de tabelas tarifárias")
@RequestMapping("/api/tabelas-tarifarias")
public interface TabelaApi {

    @Operation(summary = "Cria tabela tarifária")
    @PostMapping
    ResponseEntity<TabelaTarifaria> create(@RequestBody TabelaRequest req);

    @Operation(summary = "Lista todas as tabelas tarifárias")
    @GetMapping
    ResponseEntity<List<TabelaTarifaria>> list();

    @Operation(summary = "Deleta uma tabela por id")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id);
}

