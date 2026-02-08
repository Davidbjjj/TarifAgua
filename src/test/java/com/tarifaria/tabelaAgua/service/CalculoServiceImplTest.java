package com.tarifaria.tabelaAgua.service;

import com.tarifaria.tabelaAgua.dto.CalculoRequest;
import com.tarifaria.tabelaAgua.dto.CalculoResponse;
import com.tarifaria.tabelaAgua.model.Categoria;
import com.tarifaria.tabelaAgua.model.FaixaTarifaria;
import com.tarifaria.tabelaAgua.model.TabelaTarifaria;
import com.tarifaria.tabelaAgua.repository.FaixaTarifariaRepository;
import com.tarifaria.tabelaAgua.repository.TabelaTarifariaRepository;
import com.tarifaria.tabelaAgua.service.impl.CalculoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Testes Unitários - CalculoServiceImpl")
class CalculoServiceImplTest {

    private CalculoServiceImpl calculoService;

    @Mock
    private FaixaTarifariaRepository faixaRepo;

    @Mock
    private TabelaTarifariaRepository tabelaRepo;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        calculoService = new CalculoServiceImpl(tabelaRepo, faixaRepo);
    }

    @Test
    @DisplayName("Deve calcular corretamente faixas progressivas - Industrial com 18m³")
    void deveCalcularCorretamenteFaixasProgressivas() {
        // Arrange
        TabelaTarifaria tabela = new TabelaTarifaria(1L, "Tabela Teste", LocalDate.now(), true, null);

        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .id(1L)
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabela)
                .build();

        FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                .id(2L)
                .inicio(11)
                .fim(20)
                .valorUnitario(new BigDecimal("2.00"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabela)
                .build();

        when(tabelaRepo.findFirstByActiveTrueOrderByVigenciaDesc())
                .thenReturn(Optional.of(tabela));
        when(faixaRepo.findByTabelaIdAndCategoriaOrderByInicioAsc(1L, Categoria.INDUSTRIAL))
                .thenReturn(Arrays.asList(faixa1, faixa2));

        // Act
        CalculoResponse response = calculoService.calcular(new CalculoRequest("INDUSTRIAL", 18));

        // Assert
        assertEquals(new BigDecimal("26.00"), response.getValorTotal());
        assertEquals(Categoria.INDUSTRIAL.name(), response.getCategoria());
        assertEquals(18, response.getConsumoTotal());
        assertEquals(2, response.getDetalhamento().size());

        // Validar Faixa 1: 10m³ × R$ 1,00 = R$ 10,00
        assertEquals(10, response.getDetalhamento().get(0).getM3Cobrados());
        assertEquals(new BigDecimal("1.00"), response.getDetalhamento().get(0).getValorUnitario());
        assertEquals(new BigDecimal("10.00"), response.getDetalhamento().get(0).getSubtotal());

        // Validar Faixa 2: 8m³ × R$ 2,00 = R$ 16,00
        assertEquals(8, response.getDetalhamento().get(1).getM3Cobrados());
        assertEquals(new BigDecimal("2.00"), response.getDetalhamento().get(1).getValorUnitario());
        assertEquals(new BigDecimal("16.00"), response.getDetalhamento().get(1).getSubtotal());
    }

    @Test
    @DisplayName("Deve calcular consumo exato na primeira faixa")
    void deveCalcularConsumoExatoNaPrimeiraFaixa() {
        // Arrange
        TabelaTarifaria tabela = new TabelaTarifaria(1L, "Tabela Teste", LocalDate.now(), true, null);

        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .id(1L)
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.50"))
                .categoria(Categoria.PARTICULAR)
                .tabela(tabela)
                .build();

        when(tabelaRepo.findFirstByActiveTrueOrderByVigenciaDesc())
                .thenReturn(Optional.of(tabela));
        when(faixaRepo.findByTabelaIdAndCategoriaOrderByInicioAsc(1L, Categoria.PARTICULAR))
                .thenReturn(Arrays.asList(faixa1));

        // Act
        CalculoResponse response = calculoService.calcular(new CalculoRequest("PARTICULAR", 10));

        // Assert
        assertEquals(new BigDecimal("15.00"), response.getValorTotal());
        assertEquals(1, response.getDetalhamento().size());
        assertEquals(10, response.getDetalhamento().get(0).getM3Cobrados());
    }

    @Test
    @DisplayName("Deve calcular consumo em múltiplas faixas - 35m³")
    void deveCalcularConsumoEmMultiplasFaixas() {
        // Arrange
        TabelaTarifaria tabela = new TabelaTarifaria(1L, "Tabela Teste", LocalDate.now(), true, null);

        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .id(1L)
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.COMERCIAL)
                .tabela(tabela)
                .build();

        FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                .id(2L)
                .inicio(11)
                .fim(20)
                .valorUnitario(new BigDecimal("2.00"))
                .categoria(Categoria.COMERCIAL)
                .tabela(tabela)
                .build();

        FaixaTarifaria faixa3 = FaixaTarifaria.builder()
                .id(3L)
                .inicio(21)
                .fim(99999)
                .valorUnitario(new BigDecimal("3.00"))
                .categoria(Categoria.COMERCIAL)
                .tabela(tabela)
                .build();

        when(tabelaRepo.findFirstByActiveTrueOrderByVigenciaDesc())
                .thenReturn(Optional.of(tabela));
        when(faixaRepo.findByTabelaIdAndCategoriaOrderByInicioAsc(1L, Categoria.COMERCIAL))
                .thenReturn(Arrays.asList(faixa1, faixa2, faixa3));

        // Act
        CalculoResponse response = calculoService.calcular(new CalculoRequest("COMERCIAL", 35));

        // Assert
        // (10 × 1.00) + (10 × 2.00) + (15 × 3.00) = 10 + 20 + 45 = 75
        assertEquals(new BigDecimal("75.00"), response.getValorTotal());
        assertEquals(3, response.getDetalhamento().size());

        assertEquals(10, response.getDetalhamento().get(0).getM3Cobrados());
        assertEquals(new BigDecimal("10.00"), response.getDetalhamento().get(0).getSubtotal());

        assertEquals(10, response.getDetalhamento().get(1).getM3Cobrados());
        assertEquals(new BigDecimal("20.00"), response.getDetalhamento().get(1).getSubtotal());

        assertEquals(15, response.getDetalhamento().get(2).getM3Cobrados());
        assertEquals(new BigDecimal("45.00"), response.getDetalhamento().get(2).getSubtotal());
    }

    @Test
    @DisplayName("Deve calcular corretamente consumo baixo (5m³)")
    void deveCalcularConsumoBaixo() {
        // Arrange
        TabelaTarifaria tabela = new TabelaTarifaria(1L, "Tabela Teste", LocalDate.now(), true, null);

        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .id(1L)
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("2.50"))
                .categoria(Categoria.PUBLICO)
                .tabela(tabela)
                .build();

        when(tabelaRepo.findFirstByActiveTrueOrderByVigenciaDesc())
                .thenReturn(Optional.of(tabela));
        when(faixaRepo.findByTabelaIdAndCategoriaOrderByInicioAsc(1L, Categoria.PUBLICO))
                .thenReturn(Arrays.asList(faixa1));

        // Act
        CalculoResponse response = calculoService.calcular(new CalculoRequest("PUBLICO", 5));

        // Assert
        assertEquals(new BigDecimal("12.50"), response.getValorTotal());
        assertEquals(1, response.getDetalhamento().size());
        assertEquals(5, response.getDetalhamento().get(0).getM3Cobrados());
    }

    @Test
    @DisplayName("Deve lançar exceção quando tabela não está ativa")
    void deveLancarExcecaoQuandoTabelaNaoEstaAtiva() {
        // Arrange
        when(tabelaRepo.findFirstByActiveTrueOrderByVigenciaDesc())
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                calculoService.calcular(new CalculoRequest("INDUSTRIAL", 18))
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando categoria não tem faixas cadastradas")
    void deveLancarExcecaoQuandoCategoriaEmBranco() {
        // Arrange
        TabelaTarifaria tabela = new TabelaTarifaria(1L, "Tabela Teste", LocalDate.now(), true, null);

        when(tabelaRepo.findFirstByActiveTrueOrderByVigenciaDesc())
                .thenReturn(Optional.of(tabela));
        when(faixaRepo.findByTabelaIdAndCategoriaOrderByInicioAsc(1L, Categoria.INDUSTRIAL))
                .thenReturn(Arrays.asList());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                calculoService.calcular(new CalculoRequest("INDUSTRIAL", 18))
        );
    }

    @Test
    @DisplayName("Deve calcular corretamente com valores decimais nas tarifas")
    void deveCalcularComValoresDecimais() {
        // Arrange
        TabelaTarifaria tabela = new TabelaTarifaria(1L, "Tabela Teste", LocalDate.now(), true, null);

        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .id(1L)
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.75"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabela)
                .build();

        FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                .id(2L)
                .inicio(11)
                .fim(20)
                .valorUnitario(new BigDecimal("2.35"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabela)
                .build();

        when(tabelaRepo.findFirstByActiveTrueOrderByVigenciaDesc())
                .thenReturn(Optional.of(tabela));
        when(faixaRepo.findByTabelaIdAndCategoriaOrderByInicioAsc(1L, Categoria.INDUSTRIAL))
                .thenReturn(Arrays.asList(faixa1, faixa2));

        // Act
        CalculoResponse response = calculoService.calcular(new CalculoRequest("INDUSTRIAL", 15));

        // Assert
        // Faixa 1: 10 × 1.75 = 17.50
        // Faixa 2: 5 × 2.35 = 11.75
        // Total: 29.25
        assertEquals(new BigDecimal("29.25"), response.getValorTotal());
        assertEquals(new BigDecimal("17.50"), response.getDetalhamento().get(0).getSubtotal());
        assertEquals(new BigDecimal("11.75"), response.getDetalhamento().get(1).getSubtotal());
    }

    @Test
    @DisplayName("Deve consumir apenas até o limite da faixa quando passa por ela")
    void deveRespeitarLimiteDaFaixa() {
        // Arrange
        TabelaTarifaria tabela = new TabelaTarifaria(1L, "Tabela Teste", LocalDate.now(), true, null);

        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .id(1L)
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.PARTICULAR)
                .tabela(tabela)
                .build();

        FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                .id(2L)
                .inicio(11)
                .fim(20)
                .valorUnitario(new BigDecimal("2.00"))
                .categoria(Categoria.PARTICULAR)
                .tabela(tabela)
                .build();

        when(tabelaRepo.findFirstByActiveTrueOrderByVigenciaDesc())
                .thenReturn(Optional.of(tabela));
        when(faixaRepo.findByTabelaIdAndCategoriaOrderByInicioAsc(1L, Categoria.PARTICULAR))
                .thenReturn(Arrays.asList(faixa1, faixa2));

        // Act
        CalculoResponse response = calculoService.calcular(new CalculoRequest("PARTICULAR", 12));

        // Assert
        // Faixa 1: 10m³ (0-10)
        // Faixa 2: 2m³ (11-12)
        assertEquals(10, response.getDetalhamento().get(0).getM3Cobrados());
        assertEquals(2, response.getDetalhamento().get(1).getM3Cobrados());
        assertEquals(new BigDecimal("14.00"), response.getValorTotal());
    }
}

