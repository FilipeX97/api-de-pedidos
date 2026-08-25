package br.com.api.pedidos.observability.metrics;

import br.com.api.pedidos.payment.entity.FormaPagamento;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricasPagamentoServiceTest {

    private SimpleMeterRegistry meterRegistry;
    private MetricasPagamentoService metricasPagamentoService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        metricasPagamentoService =
                new MetricasPagamentoService(
                        meterRegistry
                );
    }

    @AfterEach
    void tearDown() {
        meterRegistry.close();
    }

    @Test
    void deveIncrementarPagamentosIniciadosPorForma() {
        metricasPagamentoService
                .registrarPagamentoIniciado(
                        FormaPagamento.PIX
                );

        metricasPagamentoService
                .registrarPagamentoIniciado(
                        FormaPagamento.PIX
                );

        metricasPagamentoService
                .registrarPagamentoIniciado(
                        FormaPagamento.BOLETO
                );

        double quantidadePix = buscarContador(
                "api.pedidos.pagamentos.iniciados",
                "pix"
        );

        double quantidadeBoleto = buscarContador(
                "api.pedidos.pagamentos.iniciados",
                "boleto"
        );

        double quantidadeCartao = buscarContador(
                "api.pedidos.pagamentos.iniciados",
                "cartao_credito"
        );

        assertAll(
                () -> assertEquals(
                        2.0,
                        quantidadePix
                ),
                () -> assertEquals(
                        1.0,
                        quantidadeBoleto
                ),
                () -> assertEquals(
                        0.0,
                        quantidadeCartao
                )
        );
    }

    @Test
    void deveIncrementarPagamentoAprovado() {
        metricasPagamentoService
                .registrarPagamentoAprovado(
                        FormaPagamento.CARTAO_CREDITO
                );

        double quantidade = buscarContador(
                "api.pedidos.pagamentos.aprovados",
                "cartao_credito"
        );

        assertEquals(
                1.0,
                quantidade
        );
    }

    @Test
    void deveIncrementarPagamentoRecusado() {
        metricasPagamentoService
                .registrarPagamentoRecusado(
                        FormaPagamento.BOLETO
                );

        double quantidade = buscarContador(
                "api.pedidos.pagamentos.recusados",
                "boleto"
        );

        assertEquals(
                1.0,
                quantidade
        );
    }

    @Test
    void deveIncrementarPagamentoPendente() {
        metricasPagamentoService
                .registrarPagamentoPendente(
                        FormaPagamento.PIX
                );

        double quantidade = buscarContador(
                "api.pedidos.pagamentos.pendentes",
                "pix"
        );

        assertEquals(
                1.0,
                quantidade
        );
    }

    @Test
    void naoDeveRegistrarMetricaSemFormaDePagamento() {
        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metricasPagamentoService
                                .registrarPagamentoIniciado(
                                        null
                                )
                );

        assertEquals(
                "Forma de pagamento é obrigatória "
                        + "para registrar a métrica",
                excecao.getMessage()
        );
    }

    private double buscarContador(
            String nomeMetrica,
            String formaPagamento
    ) {
        return meterRegistry
                .get(nomeMetrica)
                .tag(
                        "forma_pagamento",
                        formaPagamento
                )
                .counter()
                .count();
    }
}
