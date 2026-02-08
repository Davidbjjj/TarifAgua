package com.tarifaria.tabelaAgua.service;

import com.tarifaria.tabelaAgua.dto.TabelaRequest;
import com.tarifaria.tabelaAgua.model.TabelaTarifaria;
import com.tarifaria.tabelaAgua.model.FaixaTarifaria;

import java.util.List;

public interface TabelaService {
    TabelaTarifaria createTabela(TabelaRequest req);
    List<TabelaTarifaria> listAll();
    void deleteTabela(Long id);
    List<FaixaTarifaria> listFaixasByCategoria(String categoria);
}


