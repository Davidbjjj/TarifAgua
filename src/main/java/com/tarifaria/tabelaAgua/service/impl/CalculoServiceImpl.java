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

    public CalculoResponse calcular(CalculoRequest req) {
        Categoria categoria = Categoria.valueOf(req.getCategoria());
        Integer consumo = req.getConsumo();
        TabelaTarifaria tabela = tabelaRepo.findFirstByActiveTrueOrderByVigenciaDesc()
                .orElseThrow(() -> new IllegalStateException("Nenhuma tabela ativa encontrada"));

        List<FaixaTarifaria> faixas = faixaRepo.findByTabelaIdAndCategoriaOrderByInicioAsc(tabela.getId(), categoria);
        if (faixas.isEmpty()) throw new IllegalArgumentException("Nenhuma faixa cadastrada para categoria " + categoria);

        BigDecimal valorTotal = BigDecimal.ZERO;
        List<CalculoResponse.Item> detalhamento = new ArrayList<>();

        for (FaixaTarifaria f : faixas) {
            int m3Cobrados = 0;
            if (f.getInicio() == 0) {
                m3Cobrados = Math.max(0, Math.min(consumo, f.getFim()) - 0);
            } else {
                m3Cobrados = Math.max(0, Math.min(consumo, f.getFim()) - (f.getInicio() - 1));
            }
            if (m3Cobrados > 0) {
                BigDecimal subtotal = f.getValorUnitario().multiply(BigDecimal.valueOf(m3Cobrados));
                valorTotal = valorTotal.add(subtotal);
                CalculoResponse.Item item = new CalculoResponse.Item(
                        new CalculoResponse.Faixa(f.getInicio(), f.getFim()),
                        m3Cobrados,
                        f.getValorUnitario(),
                        subtotal
                );
                detalhamento.add(item);
            }
            if (consumo <= f.getFim()) break;
        }

        return new CalculoResponse(categoria.name(), consumo, valorTotal, detalhamento);
    }
}
