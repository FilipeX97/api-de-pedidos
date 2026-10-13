package br.com.api.pedidos.messaging.outbox.listener;

import br.com.api.pedidos.messaging.dto.PedidoEventoKafkaMensagem;
import br.com.api.pedidos.messaging.outbox.service.OutboxEventoService;
import br.com.api.pedidos.order.event.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class KafkaPedidoOutboxListener {

    private final OutboxEventoService outboxEventoService;

    public KafkaPedidoOutboxListener(OutboxEventoService outboxEventoService) {
        this.outboxEventoService = outboxEventoService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT
    )
    public void aoPagarPedido(PedidoPagoEvent event) {
        outboxEventoService.registrar(
                criarMensagem(
                        "PEDIDO_PAGO",
                        event.idPedido(),
                        event.idUsuario(),
                        event.valorFinal(),
                        event.dataHoraPagamento(),
                        "PAGO"
                )
        );
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT
    )
    public void aoEventoPedido(PedidoEnviadoEvent event) {
        outboxEventoService.registrar(
                criarMensagem(
                        "PEDIDO_ENVIADO",
                        event.idPedido(),
                        event.idUsuario(),
                        null,
                        event.dataHoraEnvio(),
                        event.statusNovo().name()
                )
        );
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT
    )
    public void aoEntregarPedido(PedidoEntregueEvent event) {
        outboxEventoService.registrar(
                criarMensagem(
                        "PEDIDO_ENTREGUE",
                        event.idPedido(),
                        event.idUsuario(),
                        event.valorFinal(),
                        event.dataHoraEntrega(),
                        "ENTREGUE"
                )
        );
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT
    )
    public void aoCancelarPedido(PedidoCanceladoEvent event) {
        outboxEventoService.registrar(
                criarMensagem(
                        "PEDIDO_CANCELADO",
                        event.idPedido(),
                        event.idUsuario(),
                        null,
                        event.dataHoraCancelamento(),
                        event.statusNovo().name()
                )
        );
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT
    )
    public void aoEstornarPedido(PedidoEstornadoEvent event) {
        outboxEventoService.registrar(
                criarMensagem(
                        "PEDIDO_ESTORNADO",
                        event.idPedido(),
                        event.idUsuario(),
                        event.valorFinal(),
                        event.dataHoraEstorno(),
                        "ESTORNADO"
                )
        );
    }

    private PedidoEventoKafkaMensagem criarMensagem(
            String tipoEvento,
            Long idPedido,
            Long idUsuario,
            BigDecimal valorFinal,
            LocalDateTime dataHoraEvento,
            String statusNovo
    ) {
        return new PedidoEventoKafkaMensagem(
                UUID.randomUUID(),
                1,
                tipoEvento,
                idPedido,
                idUsuario,
                valorFinal,
                dataHoraEvento,
                statusNovo
        );
    }
}
