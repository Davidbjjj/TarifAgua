package com.tarifaria.tabelaAgua.swagger;

import com.tarifaria.tabelaAgua.dto.TabelaRequest;
import com.tarifaria.tabelaAgua.model.TabelaTarifaria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tabela Tarifaria", description = "CRUD de tabelas tarifárias")
@RequestMapping("/api/tabelas-tarifarias")
public interface TabelaApi {

    @Operation(summary = "Cria tabela tarifária")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tabela criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    ResponseEntity<TabelaTarifaria> create(@RequestBody TabelaRequest req);

    @Operation(summary = "Lista todas as tabelas tarifárias")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    ResponseEntity<List<TabelaTarifaria>> list();

    @Operation(summary = "Deleta uma tabela por id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tabela deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Tabela não encontrada")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id);
}

