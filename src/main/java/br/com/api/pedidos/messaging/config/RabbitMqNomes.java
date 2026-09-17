package br.com.api.pedidos.messaging.config;

public final class RabbitMqNomes {

    private RabbitMqNomes() {}

    public static final String EXCHANGE_EVENTOS = "api.pedidos.events";
    public static final String EXCHANGE_DEAD_LETTER = "api.pedidos.dlx";
    public static final String FILA_NOTIFICACOES_PEDIDO = "notificacoes.pedido";
    public static final String FILA_NOTIFICACOES_PEDIDO_DLQ = "notificacoes.pedido.dlq";
    public static final String ROUTING_KEY_PEDIDO_CRIADO = "pedido.criado";
    public static final String ROUTING_KEY_PEDIDO_PAGO = "pedido.pago";
    public static final String ROUTING_KEY_PEDIDO_ENVIADO = "pedido.enviado";
    public static final String ROUTING_KEY_PEDIDO_ENTREGUE = "pedido.entregue";
    public static final String ROUTING_KEY_PEDIDO_CANCELADO = "pedido.cancelado";
    public static final String ROUTING_KEY_PEDIDO_ESTORNADO = "pedido.estornado";

}
