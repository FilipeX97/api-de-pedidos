package br.com.api.pedidos.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricasPedidoService  {

    private static final String METRICA_PEDIDOS_CRIADOS =
            "api.pedidos.pedidos.criados";

    private final Counter pedidosCriados;

    public MetricasPedidoService(MeterRegistry meterRegistry) {
        this.pedidosCriados = Counter
                .builder(METRICA_PEDIDOS_CRIADOS)
                .description("Quantidade total de pedidos criados")
                .register(meterRegistry);
    }

    public void registrarPedidoCriado() {
        pedidosCriados.increment();
    }
}
