package com.tarifaria.tabelaAgua.service;

import com.tarifaria.tabelaAgua.dto.TabelaRequest;
import com.tarifaria.tabelaAgua.model.TabelaTarifaria;

import java.util.List;

public interface TabelaService {
    TabelaTarifaria createTabela(TabelaRequest req);
    List<TabelaTarifaria> listAll();
    void deleteTabela(Long id);
}

