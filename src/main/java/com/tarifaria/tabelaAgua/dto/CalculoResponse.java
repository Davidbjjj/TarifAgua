package com.tarifaria.tabelaAgua.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculoResponse {
    private String categoria;
    private Integer consumoTotal;
    private BigDecimal valorTotal;
    private List<Item> detalhamento;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Faixa faixa;
        private Integer m3Cobrados;
        private BigDecimal valorUnitario;
        private BigDecimal subtotal;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Faixa {
        private Integer inicio;
        private Integer fim;
    }
}

