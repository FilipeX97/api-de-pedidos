package br.com.api.pedidos.order.state;

public enum StatusPedido {
    CRIADO,
    AGUARDANDO_PAGAMENTO,
    PAGO,
    ENVIADO,
    ENTREGUE,
    CANCELAMENTO_SOLICITADO,
    ESTORNADO,
    CANCELADO
}
