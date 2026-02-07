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

    @Override
    @Transactional
    public TabelaTarifaria createTabela(TabelaRequest req) {
        final TabelaTarifaria tabela = construirTabela(req);
        final Set<FaixaTarifaria> faixas = construirFaixasDeTabela(req, tabela);

        tabela.setFaixas(faixas);

        if (tabela.isActive()) {
            desativarTabelasAnteriores();
        }

        return tabelaRepo.save(tabela);
    }

    private TabelaTarifaria construirTabela(TabelaRequest req) {
        return TabelaTarifaria.builder()
                .nome(req.getNome())
                .vigencia(req.getVigencia())
                .active(req.isActive())
                .build();
    }

    private Set<FaixaTarifaria> construirFaixasDeTabela(TabelaRequest req, TabelaTarifaria tabela) {
        final Set<FaixaTarifaria> faixas = new HashSet<>();

        for (final TabelaRequest.CategoriaDto cat : req.getCategorias()) {
            final Categoria categoria = Categoria.valueOf(cat.getCategoria());
            final List<TabelaRequest.FaixaDto> faixasOrdenadas = ordenarFaixas(cat.getFaixas());

            validarFaixasCategoria(faixasOrdenadas, categoria);
            adicionarFaixasNaTabela(faixas, faixasOrdenadas, tabela, categoria);
        }

        return faixas;
    }

    private List<TabelaRequest.FaixaDto> ordenarFaixas(List<TabelaRequest.FaixaDto> faixas) {
        return faixas.stream()
                .sorted(Comparator.comparingInt(TabelaRequest.FaixaDto::getInicio))
                .collect(Collectors.toList());
    }

    private void validarFaixasCategoria(List<TabelaRequest.FaixaDto> faixas, Categoria categoria) {
        if (faixas.isEmpty()) {
            throw new IllegalArgumentException("Categorias devem conter faixas: " + categoria);
        }

        validarPrimeiraFaixa(faixas.getFirst(), categoria);
        validarContinuidadeFaixas(faixas, categoria);
        validarCoberturaFinal(faixas.getLast(), categoria);
    }

    private void validarPrimeiraFaixa(TabelaRequest.FaixaDto faixa, Categoria categoria) {
        if (!Objects.equals(faixa.getInicio(), 0)) {
            throw new IllegalArgumentException("Primeira faixa de " + categoria + " deve iniciar em 0");
        }
    }

    private void validarContinuidadeFaixas(List<TabelaRequest.FaixaDto> faixas, Categoria categoria) {
        for (int i = 0; i < faixas.size(); i++) {
            final TabelaRequest.FaixaDto faixa = faixas.get(i);

            if (faixa.getInicio() >= faixa.getFim()) {
                throw new IllegalArgumentException("inicio deve ser < fim em faixa");
            }

            if (i > 0) {
                final TabelaRequest.FaixaDto faixaAnterior = faixas.get(i - 1);
                if (!Objects.equals(faixa.getInicio(), faixaAnterior.getFim() + 1)) {
                    throw new IllegalArgumentException("Faixas devem ser contínuas e não sobrepor para " + categoria);
                }
            }
        }
    }

    private void validarCoberturaFinal(TabelaRequest.FaixaDto ultimaFaixa, Categoria categoria) {
        final int COBERTURA_MINIMA = 99999;
        if (ultimaFaixa.getFim() < COBERTURA_MINIMA) {
            throw new IllegalArgumentException("Cobertura insuficiente para " + categoria +
                    ", último fim deve ser >= " + COBERTURA_MINIMA);
        }
    }

    private void adicionarFaixasNaTabela(Set<FaixaTarifaria> faixas,
                                        List<TabelaRequest.FaixaDto> faixasDto,
                                        TabelaTarifaria tabela,
                                        Categoria categoria) {
        for (final TabelaRequest.FaixaDto faixaDto : faixasDto) {
            final FaixaTarifaria faixa = FaixaTarifaria.builder()
                    .categoria(categoria)
                    .inicio(faixaDto.getInicio())
                    .fim(faixaDto.getFim())
                    .valorUnitario(faixaDto.getValorUnitario())
                    .tabela(tabela)
                    .build();
            faixas.add(faixa);
        }
    }

    private void desativarTabelasAnteriores() {
        tabelaRepo.findAll().stream()
                .filter(TabelaTarifaria::isActive)
                .forEach(tabela -> {
                    tabela.setActive(false);
                    tabelaRepo.save(tabela);
                });
    }

    @Override
    public List<TabelaTarifaria> listAll() {
        return tabelaRepo.findAll();
    }

    @Override
    @Transactional
    public void deleteTabela(Long id) {
        tabelaRepo.findById(id).ifPresent(tabela -> {
            tabela.setActive(false);
            tabelaRepo.save(tabela);
        });
    }
}

