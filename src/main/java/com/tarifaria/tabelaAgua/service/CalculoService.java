package com.tarifaria.tabelaAgua.service;

import com.tarifaria.tabelaAgua.dto.CalculoRequest;
import com.tarifaria.tabelaAgua.dto.CalculoResponse;

public interface CalculoService {
    CalculoResponse calcular(CalculoRequest req);
}

