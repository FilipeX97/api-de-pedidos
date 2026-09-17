package br.com.api.pedidos.order.listener;

import br.com.api.pedidos.messaging.config.RabbitMqNomes;
import br.com.api.pedidos.messaging.dto.PedidoEventoMensagem;
import br.com.api.pedidos.messaging.producer.PedidoEventoProducer;
import br.com.api.pedidos.order.event.PedidoCanceladoEvent;
import br.com.api.pedidos.order.event.PedidoEntregueEvent;
import br.com.api.pedidos.order.event.PedidoEstornadoEvent;
import br.com.api.pedidos.order.event.PedidoEnviadoEvent;
import br.com.api.pedidos.order.event.PedidoPagoEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
public class NotificacaoPedidoListener {

    private final PedidoEventoProducer pedidoEventoProducer;

    public NotificacaoPedidoListener(
            PedidoEventoProducer pedidoEventoProducer
    ) {
        this.pedidoEventoProducer = pedidoEventoProducer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoPagarPedido(PedidoPagoEvent event) {
        pedidoEventoProducer.publicar(
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "PEDIDO_PAGO",
                        event.idPedido(),
                        event.idUsuario(),
                        event.valorFinal(),
                        event.dataHoraPagamento()
                ),
                RabbitMqNomes.ROUTING_KEY_PEDIDO_PAGO
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEnviarPedido(PedidoEnviadoEvent event) {
        pedidoEventoProducer.publicar(
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "PEDIDO_ENVIADO",
                        event.idPedido(),
                        event.idUsuario(),
                        null,
                        event.dataHoraEnvio(),
                        event.statusNovo().name()
                ),
                RabbitMqNomes.ROUTING_KEY_PEDIDO_ENVIADO
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEntregarPedido(PedidoEntregueEvent event) {
        pedidoEventoProducer.publicar(
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "PEDIDO_ENTREGUE",
                        event.idPedido(),
                        event.idUsuario(),
                        event.valorFinal(),
                        event.dataHoraEntrega()
                ),
                RabbitMqNomes.ROUTING_KEY_PEDIDO_ENTREGUE
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCancelarPedido(PedidoCanceladoEvent event) {
        pedidoEventoProducer.publicar(
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "PEDIDO_CANCELADO",
                        event.idPedido(),
                        event.idUsuario(),
                        null,
                        event.dataHoraCancelamento(),
                        event.statusNovo().name()
                ),
                RabbitMqNomes.ROUTING_KEY_PEDIDO_CANCELADO
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEstornarPedido(PedidoEstornadoEvent event) {
        pedidoEventoProducer.publicar(
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "PEDIDO_ESTORNADO",
                        event.idPedido(),
                        event.idUsuario(),
                        event.valorFinal(),
                        event.dataHoraEstorno()
                ),
                RabbitMqNomes.ROUTING_KEY_PEDIDO_ESTORNADO
        );
    }
}
