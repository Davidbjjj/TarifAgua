package com.tarifaria.tabelaAgua.service;

import com.tarifaria.tabelaAgua.dto.CalculoRequest;
import com.tarifaria.tabelaAgua.dto.CalculoResponse;
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
@DisplayName("Testes de Integração - CalculoService com Banco de Dados")
class CalculoServiceIntegrationTest {

    @Autowired
    private CalculoService calculoService;

    @Autowired
    private TabelaTarifariaRepository tabelaRepo;

    @Autowired
    private FaixaTarifariaRepository faixaRepo;

    private TabelaTarifaria tabelaAtiva;

    @BeforeEach
    void setup() {
        // Cria tabela tarifária ativa
        tabelaAtiva = new TabelaTarifaria();
        tabelaAtiva.setNome("Tabela Ativa - Testes");
        tabelaAtiva.setVigencia(LocalDate.now());
        tabelaAtiva.setActive(true);
        tabelaAtiva = tabelaRepo.save(tabelaAtiva);
    }

    @Test
    @DisplayName("Deve calcular com dados reais do banco - Industrial com 18m³")
    void deveCalcularComDadosRealsDoBanco_Industrial() {
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

        faixaRepo.saveAll(Arrays.asList(faixa1, faixa2));

        // Act
        CalculoResponse response = calculoService.calcular(new CalculoRequest("INDUSTRIAL", 18));

        // Assert
        assertEquals(new BigDecimal("26.00"), response.getValorTotal());
        assertEquals(18, response.getConsumoTotal());
        assertEquals("INDUSTRIAL", response.getCategoria());
        assertEquals(2, response.getDetalhamento().size());
    }

    @Test
    @DisplayName("Deve calcular com dados reais - Comercial com 35m³")
    void deveCalcularComDadosReais_Comercial() {
        // Arrange - Cria faixas para categoria COMERCIAL
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

        FaixaTarifaria faixa3 = FaixaTarifaria.builder()
                .inicio(21)
                .fim(99999)
                .valorUnitario(new BigDecimal("3.00"))
                .categoria(Categoria.COMERCIAL)
                .tabela(tabelaAtiva)
                .build();

        faixaRepo.saveAll(Arrays.asList(faixa1, faixa2, faixa3));

        // Act
        CalculoResponse response = calculoService.calcular(new CalculoRequest("COMERCIAL", 35));

        // Assert
        // (10 × 1.00) + (10 × 2.00) + (15 × 3.00) = 75
        assertEquals(new BigDecimal("75.00"), response.getValorTotal());
        assertEquals(3, response.getDetalhamento().size());
    }

    @Test
    @DisplayName("Deve refletir automaticamente alterações no banco de dados")
    void deveRefletirAlteracoesNoBancoDados() {
        // Arrange - Cria faixas iniciais
        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.PARTICULAR)
                .tabela(tabelaAtiva)
                .build();

        FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                .inicio(11)
                .fim(20)
                .valorUnitario(new BigDecimal("2.00"))
                .categoria(Categoria.PARTICULAR)
                .tabela(tabelaAtiva)
                .build();

        faixa1 = faixaRepo.save(faixa1);
        faixa2 = faixaRepo.save(faixa2);

        // Act - Primeira chamada com valores originais
        CalculoResponse response1 = calculoService.calcular(new CalculoRequest("PARTICULAR", 15));
        BigDecimal valorOriginal = response1.getValorTotal();
        // (10 × 1.00) + (5 × 2.00) = 20.00
        assertEquals(new BigDecimal("20.00"), valorOriginal);

        // Atualiza valor unitário da faixa 2 no banco
        faixa2.setValorUnitario(new BigDecimal("3.50"));
        faixaRepo.save(faixa2);

        // Act - Segunda chamada com novo valor
        CalculoResponse response2 = calculoService.calcular(new CalculoRequest("PARTICULAR", 15));
        BigDecimal valorAtualizado = response2.getValorTotal();
        // (10 × 1.00) + (5 × 3.50) = 27.50
        assertEquals(new BigDecimal("27.50"), valorAtualizado);

        // Assert - Valores devem ser diferentes
        assertNotEquals(valorOriginal, valorAtualizado);
        assertTrue(valorAtualizado.compareTo(valorOriginal) > 0);
    }

    @Test
    @DisplayName("Deve calcular todas as categorias com dados do banco")
    void deveCalcularTodoasAsCategorias() {
        // Arrange - Cria faixas para cada categoria
        Categoria[] categorias = {Categoria.COMERCIAL, Categoria.INDUSTRIAL, Categoria.PARTICULAR, Categoria.PUBLICO};

        for (Categoria categoria : categorias) {
            FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                    .inicio(0)
                    .fim(10)
                    .valorUnitario(new BigDecimal("1.00"))
                    .categoria(categoria)
                    .tabela(tabelaAtiva)
                    .build();

            FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                    .inicio(11)
                    .fim(20)
                    .valorUnitario(new BigDecimal("2.00"))
                    .categoria(categoria)
                    .tabela(tabelaAtiva)
                    .build();

            faixaRepo.saveAll(Arrays.asList(faixa1, faixa2));
        }

        // Act & Assert - Calcula para cada categoria
        for (Categoria categoria : categorias) {
            CalculoResponse response = calculoService.calcular(new CalculoRequest(categoria.name(), 18));
            assertEquals(categoria.name(), response.getCategoria());
            assertEquals(new BigDecimal("26.00"), response.getValorTotal());
        }
    }

    @Test
    @DisplayName("Deve calcular corretamente com múltiplas tabelas (apenas a ativa)")
    void deveCalcularApenasComTabelaAtiva() {
        // Arrange - Cria duas tabelas, apenas uma ativa
        TabelaTarifaria tabelaInativa = new TabelaTarifaria();
        tabelaInativa.setNome("Tabela Inativa");
        tabelaInativa.setVigencia(LocalDate.now().minusDays(30));
        tabelaInativa.setActive(false);
        tabelaInativa = tabelaRepo.save(tabelaInativa);

        // Adiciona faixa inativa (valores altos)
        FaixaTarifaria faixaInativa = FaixaTarifaria.builder()
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("99.99"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabelaInativa)
                .build();
        faixaRepo.save(faixaInativa);

        // Adiciona faixa ativa (valor correto)
        FaixaTarifaria faixaAtiva = FaixaTarifaria.builder()
                .inicio(0)
                .fim(10)
                .valorUnitario(new BigDecimal("1.50"))
                .categoria(Categoria.INDUSTRIAL)
                .tabela(tabelaAtiva)
                .build();
        faixaRepo.save(faixaAtiva);

        // Act
        CalculoResponse response = calculoService.calcular(new CalculoRequest("INDUSTRIAL", 10));

        // Assert - Deve usar apenas a tabela ativa
        assertEquals(new BigDecimal("15.00"), response.getValorTotal());
        assertNotEquals(new BigDecimal("999.90"), response.getValorTotal());
    }

    @Test
    @DisplayName("Deve calcular com faixas com diferentes intervalos")
    void deveCalcularComFaixasComDiferentesIntervalos() {
        // Arrange
        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .inicio(0)
                .fim(5)
                .valorUnitario(new BigDecimal("0.50"))
                .categoria(Categoria.PARTICULAR)
                .tabela(tabelaAtiva)
                .build();

        FaixaTarifaria faixa2 = FaixaTarifaria.builder()
                .inicio(6)
                .fim(15)
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.PARTICULAR)
                .tabela(tabelaAtiva)
                .build();

        FaixaTarifaria faixa3 = FaixaTarifaria.builder()
                .inicio(16)
                .fim(30)
                .valorUnitario(new BigDecimal("1.50"))
                .categoria(Categoria.PARTICULAR)
                .tabela(tabelaAtiva)
                .build();

        faixaRepo.saveAll(Arrays.asList(faixa1, faixa2, faixa3));

        // Act
        CalculoResponse response = calculoService.calcular(new CalculoRequest("PARTICULAR", 25));

        // Assert - 3 faixas cadastradas, consumo de 25m³
        assertEquals(3, response.getDetalhamento().size());
        // Faixa 1 (0-5): 5m³ × 0.50 = 2.50
        // Faixa 2 (6-15): 10m³ × 1.00 = 10.00
        // Faixa 3 (16-30): 10m³ × 1.50 = 15.00
        // Total: 27.50
        assertEquals(new BigDecimal("27.50"), response.getValorTotal());
    }

    @Test
    @DisplayName("Deve validar e lançar exceção quando primeira faixa não começa em 0")
    void deveValidarPrimeiraFaixaComecaEmZero() {
        // Arrange - Tenta criar faixa que não começa em 0 (será testado no serviço)
        FaixaTarifaria faixa1 = FaixaTarifaria.builder()
                .inicio(5)  // ERRADO: deveria começar em 0
                .fim(10)
                .valorUnitario(new BigDecimal("1.00"))
                .categoria(Categoria.PUBLICO)
                .tabela(tabelaAtiva)
                .build();

        faixaRepo.save(faixa1);

        // Act & Assert - A validação deveria ocorrer
        // Nota: Dependendo da implementação, isso pode ser validado no serviço ou no repositório
        assertThrows(Exception.class, () ->
                calculoService.calcular(new CalculoRequest("PUBLICO", 10))
        );
    }
}

