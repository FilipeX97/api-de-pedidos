package br.com.api.pedidos.payment.adapter.fake;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class GatewayCartaoFake {

    private static final BigDecimal LIMITE_APROVACAO =
            new BigDecimal("5000");

    public RespostaCartaoGateway autorizar(BigDecimal valor) {
        if (valor.compareTo(LIMITE_APROVACAO) < 0) {
            return new RespostaCartaoGateway(
                    true,
                    "CARD-" + UUID.randomUUID(),
                    "Pagamento autorizado pela operadora"
            );
        }

        return new RespostaCartaoGateway(
                false,
                "CARD-" + UUID.randomUUID(),
                "Pagamento recusado pela operadora"
        );
    }
}
