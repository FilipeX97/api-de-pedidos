package br.com.api.pedidos.audit.entity;

public enum TipoAcao {
    PEDIDO_CRIADO,
    PEDIDO_PAGO,
    PEDIDO_ENVIADO,
    PEDIDO_ENTREGUE,
    PEDIDO_CANCELADO,
    PEDIDO_ESTORNADO,
    CUPOM_APLICADO
}
