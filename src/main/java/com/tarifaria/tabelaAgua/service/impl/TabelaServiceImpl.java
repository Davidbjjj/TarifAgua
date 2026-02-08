package com.tarifaria.tabelaAgua.service.impl;

import com.tarifaria.tabelaAgua.dto.TabelaRequest;
import com.tarifaria.tabelaAgua.model.*;
import com.tarifaria.tabelaAgua.repository.TabelaTarifariaRepository;
import com.tarifaria.tabelaAgua.service.TabelaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TabelaServiceImpl implements TabelaService {
    private final TabelaTarifariaRepository tabelaRepo;

    @Transactional
    public TabelaTarifaria createTabela(TabelaRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Request não pode ser nulo");
        }
        if (req.getNome() == null || req.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome da tabela é obrigatório");
        }
        if (req.getCategorias() == null || req.getCategorias().isEmpty()) {
            throw new IllegalArgumentException("Pelo menos uma categoria é obrigatória");
        }

        TabelaTarifaria tabela = TabelaTarifaria.builder()
                .nome(req.getNome())
                .vigencia(req.getVigencia())
                .active(true)
                .build();

        Set<FaixaTarifaria> faixas = new HashSet<>();
        for (TabelaRequest.CategoriaDto cat : req.getCategorias()) {
            Categoria categoria = Categoria.valueOf(cat.getCategoria());

            List<TabelaRequest.FaixaDto> lista = cat.getFaixas().stream()
                    .sorted(Comparator.comparingInt(TabelaRequest.FaixaDto::getInicio))
                    .collect(Collectors.toList());

            // regra: inicio < fim, ordem válida, não sobreposição, início=0, cobertura suficiente
            if (lista.isEmpty()) throw new IllegalArgumentException("Categorias devem conter faixas: " + categoria);
            if (!Objects.equals(lista.get(0).getInicio(), 0)) {
                throw new IllegalArgumentException("Primeira faixa de " + categoria + " deve iniciar em 0");
            }
            for (int i = 0; i < lista.size(); i++) {
                TabelaRequest.FaixaDto f = lista.get(i);
                if (f.getInicio() >= f.getFim()) throw new IllegalArgumentException("inicio deve ser < fim em faixa");
                if (i > 0) {
                    TabelaRequest.FaixaDto prev = lista.get(i - 1);
                    if (!Objects.equals(f.getInicio(), prev.getFim() + 1)) {
                        throw new IllegalArgumentException("Faixas devem ser contínuas e não sobrepor para " + categoria);
                    }
                }
            }
            // cobertura suficiente: último fim >= 99999
            TabelaRequest.FaixaDto last = lista.get(lista.size() - 1);
            if (last.getFim() < 99999) throw new IllegalArgumentException("Cobertura insuficiente para " + categoria + ", último fim deve ser >= 99999");

            for (TabelaRequest.FaixaDto f : lista) {
                FaixaTarifaria faixa = FaixaTarifaria.builder()
                        .categoria(categoria)
                        .inicio(f.getInicio())
                        .fim(f.getFim())
                        .valorUnitario(f.getValorUnitario())
                        .tabela(tabela)
                        .build();
                faixas.add(faixa);
            }
        }
        tabela.setFaixas(faixas);

        // Se a nova tabela está ativa, desativar todas as outras
        if (tabela.getActive()) {
            tabelaRepo.findAll().stream()
                    .filter(TabelaTarifaria::getActive)
                    .forEach(t -> {
                        t.setActive(false);
                        tabelaRepo.save(t);
                    });
        }
        return tabelaRepo.save(tabela);
    }

    public List<TabelaTarifaria> listAll() {
        return tabelaRepo.findAll();
    }

    @Transactional
    public void deleteTabela(Long id) {
        tabelaRepo.findById(id).ifPresent(t -> {
            t.setActive(false);
            tabelaRepo.save(t);
        });
    }
}

