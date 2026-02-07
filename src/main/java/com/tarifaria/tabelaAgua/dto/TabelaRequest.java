package com.tarifaria.tabelaAgua.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TabelaRequest {
    private String nome;
    private LocalDate vigencia;
    private boolean active = true;
    private List<CategoriaDto> categorias;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaDto {
        private String categoria;
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

