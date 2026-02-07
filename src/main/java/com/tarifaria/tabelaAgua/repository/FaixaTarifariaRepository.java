package com.tarifaria.tabelaAgua.repository;

import com.tarifaria.tabelaAgua.model.FaixaTarifaria;
import com.tarifaria.tabelaAgua.model.Categoria;
import com.tarifaria.tabelaAgua.model.TabelaTarifaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaixaTarifariaRepository extends JpaRepository<FaixaTarifaria, Long> {
    List<FaixaTarifaria> findByTabelaAndCategoriaOrderByInicioAsc(TabelaTarifaria tabela, Categoria categoria);

    List<FaixaTarifaria> findByTabelaIdAndCategoriaOrderByInicioAsc(Long tabelaId, Categoria categoria);
}
