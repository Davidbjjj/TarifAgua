package com.tarifaria.tabelaAgua.repository;

import com.tarifaria.tabelaAgua.model.TabelaTarifaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TabelaTarifariaRepository extends JpaRepository<TabelaTarifaria, Long> {
    Optional<TabelaTarifaria> findFirstByActiveTrueOrderByVigenciaDesc();
}

