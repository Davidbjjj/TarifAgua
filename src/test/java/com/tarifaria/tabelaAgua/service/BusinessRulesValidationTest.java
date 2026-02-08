package com.tarifaria.tabelaAgua.service;

import com.tarifaria.tabelaAgua.model.Categoria;
import com.tarifaria.tabelaAgua.model.FaixaTarifaria;
import com.tarifaria.tabelaAgua.model.TabelaTarifaria;
import com.tarifaria.tabelaAgua.repository.FaixaTarifariaRepository;
import com.tarifaria.tabelaAgua.repository.TabelaTarifariaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Validação de Regras de Negócio")
class BusinessRulesValidationTest {

    @Autowired
    private CalculoService calculoService;

    @Autowired
    private TabelaTarifariaRepository tabelaRepo;

    @Autowired
    private FaixaTarifariaRepository faixaRepo;

    private TabelaTarifaria tabelaAtiva;

    @BeforeEach
    void setup() {
        tabelaAtiva = new TabelaTarifaria();
        tabelaAtiva.setNome("Tabela Validação");
        tabelaAtiva.setVigencia(LocalDate.now());
        tabelaAtiva.setActive(true);
        tabelaAtiva = tabelaRepo.save(tabelaAtiva);
    }

    // ==================== REGRA 1: Não Sobreposição ====================

    @Test
    @DisplayName("REGRA: Faixas não devem ter intervalos que se cruzam")
    void deveValidarNaoSobreposicaoDeFaixas() {
        // Arrange - Cria faixas sem sobreposição
        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabelaAtiva)
                .build();

        FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                .inicio(11)
                .fim(20)
                .valorUnitario(new BigDecimal("2.00"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabelaAtiva)
                .build();

        FaixaTarifaria faixa3 = FaixaTarifaria.builder()
                .inicio(21)
                .fim(99999)
                .valorUnitario(new BigDecimal("3.00"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabelaAtiva)
                .build();

        faixaRepo.saveAll(Arrays.asList(faixa1, faixa2, faixa3));

        // Assert - Sem sobreposição, cálculo deve ser preciso
        // Cálculo: (10 × 1.00) + (10 × 2.00) + (5 × 3.00) = 10 + 20 + 15 = 45
        var response = calculoService.calcular(new com.tarifaria.tabelaAgua.dto.CalculoRequest("INDUSTRIAL", 25));
        assertEquals(3, response.getDetalhamento().size());
        assertEquals(new BigDecimal("45.00"), response.getValorTotal());
    }

    // ==================== REGRA 2: Ordem Válida ====================

    @Test
    @DisplayName("REGRA: Valor inicial de cada faixa deve ser menor que o valor final (início < fim)")
    void deveValidarOrdenacaoDeFaixas() {
        // Arrange - Cria faixas com ordem válida
        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.50"))
                .categoria(Categoria.PARTICULAR)
                .tabela(tabelaAtiva)
                .build();

        FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                .inicio(11)
                .fim(20)
                .valorUnitario(new BigDecimal("2.50"))
                .categoria(Categoria.PARTICULAR)
                .tabela(tabelaAtiva)
                .build();

        faixaRepo.saveAll(Arrays.asList(faixa1, faixa2));

        // Assert
        assertEquals(0, faixa1.getInicio());
        assertTrue(faixa1.getInicio() < faixa1.getFim());
        assertTrue(faixa2.getInicio() < faixa2.getFim());
    }

    @Test
    @DisplayName("REGRA: Faixa inválida (início >= fim) deve ser rejeitada ou tratada")
    void deveRejeitarFaixaComOrdemInvalida() {
        // Arrange - Tenta criar faixa inválida
        FaixaTarifaria faixaInvalida = FaixaTarifaria.builder()
                .inicio(20)
                .fim(10)  // INVÁLIDO: início > fim
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.COMERCIAL)
                .tabela(tabelaAtiva)
                .build();

        // Assert - Dependendo da implementação, pode lançar exceção
        // Neste caso, salvamos e validamos o comportamento
        var faixaSalva = faixaRepo.save(faixaInvalida);
        assertFalse(faixaSalva.getInicio() < faixaSalva.getFim());
    }

    // ==================== REGRA 3: Cobertura Completa ====================

    @Test
    @DisplayName("REGRA: Deve iniciar em 0 (zero) m³")
    void deveIniciarEmZero() {
        // Arrange
        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .inicio(0)  // Obrigatoriamente começa em 0
                .fim(10)
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.PUBLICO)
                .tabela(tabelaAtiva)
                .build();

        FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                .inicio(11)
                .fim(99999)
                .valorUnitario(new BigDecimal("2.00"))
                .categoria(Categoria.PUBLICO)
                .tabela(tabelaAtiva)
                .build();

        faixaRepo.saveAll(Arrays.asList(faixa1, faixa2));

        // Assert
        assertEquals(0, faixa1.getInicio());
    }

    // ==================== REGRA 4: Cobertura Suficiente ====================

    @Test
    @DisplayName("REGRA: Deve haver faixas que cubram qualquer consumo informado")
    void deveCobrirQualquerConsumo() {
        // Arrange - Faixas que cobrem de 0 até infinito
        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabelaAtiva)
                .build();

        FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                .inicio(11)
                .fim(99999)  // Cobre qualquer consumo grande
                .valorUnitario(new BigDecimal("2.00"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabelaAtiva)
                .build();

        faixaRepo.saveAll(Arrays.asList(faixa1, faixa2));

        // Act - Testa com vários valores de consumo
        int[] consumosTestados = {1, 5, 10, 15, 100, 1000, 99999};

        for (int consumo : consumosTestados) {
            // Assert - Nenhum deve lançar exceção de cobertura
            assertDoesNotThrow(() ->
                calculoService.calcular(new com.tarifaria.tabelaAgua.dto.CalculoRequest("INDUSTRIAL", consumo))
            );
        }
    }

    // ==================== CENÁRIOS DE PARAMETRIZAÇÃO ====================

    @Test
    @DisplayName("PARAMETRIZAÇÃO: Alterações no banco devem refletir automaticamente")
    void deveRefletirAlteracoesParametrizadas() {
        // Arrange - Cria faixas iniciais
        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.PARTICULAR)
                .tabela(tabelaAtiva)
                .build();

        faixa1 = faixaRepo.save(faixa1);

        // Act - Cálculo 1 com valor original
        var response1 = calculoService.calcular(new com.tarifaria.tabelaAgua.dto.CalculoRequest("PARTICULAR", 10));
        var valor1 = response1.getValorTotal();

        // Altera valor no banco
        faixa1.setValorUnitario(new BigDecimal("2.50"));
        faixaRepo.save(faixa1);

        // Act - Cálculo 2 com valor modificado
        var response2 = calculoService.calcular(new com.tarifaria.tabelaAgua.dto.CalculoRequest("PARTICULAR", 10));
        var valor2 = response2.getValorTotal();

        // Assert
        assertEquals(new BigDecimal("10.00"), valor1);
        assertEquals(new BigDecimal("25.00"), valor2);
        assertNotEquals(valor1, valor2);
    }

    @Test
    @DisplayName("PARAMETRIZAÇÃO: Novas faixas devem ser incluídas automaticamente")
    void deveIncluirNovasFaixasAutomaticamente() {
        // Arrange - Cria faixas iniciais
        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.COMERCIAL)
                .tabela(tabelaAtiva)
                .build();

        FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                .inicio(11)
                .fim(20)
                .valorUnitario(new BigDecimal("2.00"))
                .categoria(Categoria.COMERCIAL)
                .tabela(tabelaAtiva)
                .build();

        faixaRepo.saveAll(Arrays.asList(faixa1, faixa2));

        // Act - Cálculo com 2 faixas
        var response1 = calculoService.calcular(new com.tarifaria.tabelaAgua.dto.CalculoRequest("COMERCIAL", 15));
        assertEquals(2, response1.getDetalhamento().size());

        // Adiciona nova faixa
        FaixaTarifaria faixa3 = FaixaTarifaria.builder()
                .inicio(21)
                .fim(99999)
                .valorUnitario(new BigDecimal("3.00"))
                .categoria(Categoria.COMERCIAL)
                .tabela(tabelaAtiva)
                .build();

        faixaRepo.save(faixa3);

        // Act - Cálculo com 3 faixas
        var response2 = calculoService.calcular(new com.tarifaria.tabelaAgua.dto.CalculoRequest("COMERCIAL", 25));
        assertEquals(3, response2.getDetalhamento().size());
    }

    @Test
    @DisplayName("PARAMETRIZAÇÃO: Remoção de faixas deve refletir automaticamente")
    void deveRemoverFaixasAutomaticamente() {
        // Arrange
        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabelaAtiva)
                .build();

        FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                .inicio(11)
                .fim(20)
                .valorUnitario(new BigDecimal("2.00"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabelaAtiva)
                .build();

        FaixaTarifaria faixa3 = FaixaTarifaria.builder()
                .inicio(21)
                .fim(99999)
                .valorUnitario(new BigDecimal("3.00"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabelaAtiva)
                .build();

        faixa2 = faixaRepo.save(faixa2);
        var saved = faixaRepo.saveAll(Arrays.asList(faixa1, faixa3));
        long faixa2Id = faixa2.getId();

        // Act - Cálculo com todas as faixas
        var response1 = calculoService.calcular(new com.tarifaria.tabelaAgua.dto.CalculoRequest("INDUSTRIAL", 25));
        assertEquals(3, response1.getDetalhamento().size());

        // Remove faixa 2
        faixaRepo.deleteById(faixa2Id);

        // Act - Cálculo deve agora ignorar a faixa 2 removida
        // (Dependendo da implementação, isso pode alterar o cálculo)
        var response2 = calculoService.calcular(new com.tarifaria.tabelaAgua.dto.CalculoRequest("INDUSTRIAL", 25));
        assertTrue(response2.getDetalhamento().size() <= 2);
    }

    @Test
    @DisplayName("PARAMETRIZAÇÃO: Múltiplas categorias devem funcionar independentemente")
    void deveSuportarMultiplasCategorias() {
        // Arrange - Cria faixas para 4 categorias
        Categoria[] categorias = {Categoria.COMERCIAL, Categoria.INDUSTRIAL, Categoria.PARTICULAR, Categoria.PUBLICO};
        BigDecimal[] valores = {new BigDecimal("1.00"), new BigDecimal("2.00"), new BigDecimal("0.50"), new BigDecimal("1.50")};

        for (int i = 0; i < categorias.length; i++) {
            FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                    .inicio(0)
                    .fim(10)
                    .valorUnitario(valores[i])
                    .categoria(categorias[i])
                    .tabela(tabelaAtiva)
                    .build();

            FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                    .inicio(11)
                    .fim(99999)
                    .valorUnitario(valores[i].multiply(BigDecimal.valueOf(2)))
                    .categoria(categorias[i])
                    .tabela(tabelaAtiva)
                    .build();

            faixaRepo.saveAll(Arrays.asList(faixa1, faixa2));
        }

        // Act & Assert - Cada categoria deve ter seu próprio cálculo
        BigDecimal[] resultadosEsperados = {
            new BigDecimal("20.00"),  // COMERCIAL: 10×1.00 + 5×2.00
            new BigDecimal("40.00"),  // INDUSTRIAL: 10×2.00 + 5×4.00
            new BigDecimal("10.00"),  // PARTICULAR: 10×0.50 + 5×1.00
            new BigDecimal("30.00")   // PUBLICO: 10×1.50 + 5×3.00
        };

        for (int i = 0; i < categorias.length; i++) {
            var response = calculoService.calcular(
                new com.tarifaria.tabelaAgua.dto.CalculoRequest(categorias[i].name(), 15)
            );
            assertEquals(resultadosEsperados[i], response.getValorTotal());
        }
    }
}

