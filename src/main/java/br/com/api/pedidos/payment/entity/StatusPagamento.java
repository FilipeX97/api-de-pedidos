package br.com.api.pedidos.payment.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "StatusPagamento",
        description = """
                Estado atual do pagamento.

                Valores disponíveis:
                PENDENTE, APROVADO, RECUSADO, CANCELADO e ESTORNADO.
                """
)
public enum StatusPagamento {
    PENDENTE,
    APROVADO,
    RECUSADO,
    CANCELADO,
    ESTORNADO
}
