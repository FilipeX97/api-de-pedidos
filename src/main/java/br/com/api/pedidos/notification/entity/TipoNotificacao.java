package br.com.api.pedidos.notification.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "TipoNotificacao",
        description = """
                Evento que originou a notificação.

                Valores disponíveis:

                PEDIDO_CRIADO,
                PEDIDO_PAGO,
                PEDIDO_ENVIADO,
                PEDIDO_ENTREGUE,
                PEDIDO_CANCELADO,
                PEDIDO_ESTORNADO e
                CUPOM_APLICADO.
                """
)
public enum TipoNotificacao {
    PEDIDO_CRIADO,
    PEDIDO_PAGO,
    PEDIDO_ENVIADO,
    PEDIDO_ENTREGUE,
    PEDIDO_CANCELADO,
    PEDIDO_ESTORNADO,
    CUPOM_APLICADO
}
