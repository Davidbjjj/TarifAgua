package com.tarifaria.tabelaAgua.swagger;

import com.tarifaria.tabelaAgua.dto.CalculoRequest;
import com.tarifaria.tabelaAgua.dto.CalculoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Calculo", description = "Operações de cálculo")
@RequestMapping("/api/calculos")
public interface CalculoApi {

    @Operation(summary = "Calcula tarifa para um consumo dado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cálculo realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = CalculoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida")
    })
    @PostMapping
    ResponseEntity<CalculoResponse> calcular(@RequestBody CalculoRequest req);
}

