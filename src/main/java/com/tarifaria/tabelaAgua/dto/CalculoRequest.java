package com.tarifaria.tabelaAgua.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculoRequest {
    private String categoria;
    private Integer consumo;
}

