package com.tarifaria.tabelaAgua.service.impl;

import com.tarifaria.tabelaAgua.dto.CalculoRequest;
import com.tarifaria.tabelaAgua.dto.CalculoResponse;
import com.tarifaria.tabelaAgua.model.*;
import com.tarifaria.tabelaAgua.repository.FaixaTarifariaRepository;
import com.tarifaria.tabelaAgua.repository.TabelaTarifariaRepository;
import com.tarifaria.tabelaAgua.service.CalculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculoServiceImpl implements CalculoService {
    private final TabelaTarifariaRepository tabelaRepo;
    private final FaixaTarifariaRepository faixaRepo;

    @Override
    public CalculoResponse calcular(CalculoRequest req) {
        final Categoria categoria = Categoria.valueOf(req.getCategoria());
        final int consumo = req.getConsumo();
        final TabelaTarifaria tabela = obterTabelaAtiva();
        final List<FaixaTarifaria> faixas = obterFaixasPorCategoria(tabela.getId(), categoria);

        BigDecimal valorTotal = BigDecimal.ZERO;
        final List<CalculoResponse.Item> detalhamento = new ArrayList<>();

        for (final FaixaTarifaria faixa : faixas) {
            final int m3Cobrados = calcularM3Cobrados(faixa, consumo);

            if (m3Cobrados > 0) {
                final BigDecimal subtotal = faixa.getValorUnitario().multiply(BigDecimal.valueOf(m3Cobrados));
                valorTotal = valorTotal.add(subtotal);
                detalhamento.add(criarItem(faixa, m3Cobrados, subtotal));
            }

            if (consumo <= faixa.getFim()) break;
        }

        return new CalculoResponse(categoria.name(), consumo, valorTotal, detalhamento);
    }

    private TabelaTarifaria obterTabelaAtiva() {
        return tabelaRepo.findFirstByActiveTrueOrderByVigenciaDesc()
                .orElseThrow(() -> new IllegalStateException("Nenhuma tabela ativa encontrada"));
    }

    private List<FaixaTarifaria> obterFaixasPorCategoria(Long tabelaId, Categoria categoria) {
        final List<FaixaTarifaria> faixas = faixaRepo.findByTabelaIdAndCategoriaOrderByInicioAsc(tabelaId, categoria);
        if (faixas.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma faixa cadastrada para categoria " + categoria);
        }
        if (faixas.get(0).getInicio() != 0) {
            throw new IllegalStateException("A primeira faixa da categoria " + categoria + " deve iniciar em 0 m³, mas inicia em " + faixas.get(0).getInicio());
        }
        return faixas;
    }

    private int calcularM3Cobrados(FaixaTarifaria faixa, int consumo) {
        final int lowerBound = faixa.getInicio() == 0 ? 0 : faixa.getInicio() - 1;
        final int upperBound = faixa.getFim();
        final int covered = Math.min(consumo, upperBound) - lowerBound;
        return Math.max(0, covered);
    }

    private CalculoResponse.Item criarItem(FaixaTarifaria faixa, int m3Cobrados, BigDecimal subtotal) {
        return new CalculoResponse.Item(
                new CalculoResponse.Faixa(faixa.getInicio(), faixa.getFim()),
                m3Cobrados,
                faixa.getValorUnitario(),
                subtotal
        );
    }
}
