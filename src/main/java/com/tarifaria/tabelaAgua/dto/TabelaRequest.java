package com.tarifaria.tabelaAgua.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TabelaRequest {
    @NotBlank(message = "Nome da tabela é obrigatório")
    private String nome;

    private LocalDate vigencia;

    @NotEmpty(message = "Categorias não podem estar vazias")
    @Valid
    private List<CategoriaDto> categorias;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaDto {
        @NotBlank(message = "Categoria é obrigatória")
        private String categoria;

        @NotEmpty(message = "Faixas não podem estar vazias")
        @Valid
        private List<FaixaDto> faixas;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FaixaDto {
        private Integer inicio;
        private Integer fim;
        private java.math.BigDecimal valorUnitario;
    }
}

