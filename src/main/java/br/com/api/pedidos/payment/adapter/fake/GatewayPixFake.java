package br.com.api.pedidos.payment.adapter.fake;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class GatewayPixFake {
    public RespostaPixGateway cobrar(BigDecimal valor) {
        return new RespostaPixGateway(
                "PIX-" + UUID.randomUUID(),
                true
        );
    }
}
