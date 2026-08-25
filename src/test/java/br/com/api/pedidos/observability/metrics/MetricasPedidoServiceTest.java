package br.com.api.pedidos.observability.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricasPedidoServiceTest {

    private SimpleMeterRegistry meterRegistry;
    private MetricasPedidoService metricasPedidoService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricasPedidoService = new MetricasPedidoService(meterRegistry);
    }

    @AfterEach
    void tearDown() {
        meterRegistry.close();
    }

    @Test
    void deveIncrementarQuantidadeDePedidosCriados() {
        metricasPedidoService.registrarPedidoCriado();
        metricasPedidoService.registrarPedidoCriado();

        double quantidade = meterRegistry
                .get("api.pedidos.pedidos.criados")
                .counter()
                .count();

        assertEquals(
                2.0,
                quantidade
        );
    }
}