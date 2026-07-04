package br.com.api.pedidos.order.listener;

import br.com.api.pedidos.notification.entity.TipoNotificacao;
import br.com.api.pedidos.notification.service.NotificacaoService;
import br.com.api.pedidos.order.event.*;
import br.com.api.pedidos.order.state.StatusPedido;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificacaoPedidoListener {

    private final NotificacaoService notificacaoService;

    public NotificacaoPedidoListener(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCriarPedido(PedidoCriadoEvent event) {
        notificacaoService.criar(
                event.idPedido(),
                "Pedido criado",
                "Seu pedido #" + event.idPedido() + " foi criado com sucesso.",
                TipoNotificacao.PEDIDO_CRIADO
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoPagarPedido(PedidoPagoEvent event) {
        notificacaoService.criar(
                event.idPedido(),
                "Pagamento confirmado",
                    "O pagamento do pedido #" + event.idPedido() + " foi confirmado",
                TipoNotificacao.PEDIDO_PAGO
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEnviarPedido(PedidoEnviadoEvent event) {
        notificacaoService.criar(
                event.idPedido(),
                "Pedido enviado",
                "Seu pedido #" + event.idPedido() + " foi enviado.",
                TipoNotificacao.PEDIDO_ENVIADO
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEntregarPedido(PedidoEntregueEvent event) {
        notificacaoService.criar(
                event.idPedido(),
                "Pedido entregue",
                "Seu pedido #" + event.idPedido() + " foi entregue.",
                TipoNotificacao.PEDIDO_ENTREGUE
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCancelarPedido(PedidoCanceladoEvent event) {
        String titulo = event.statusNovo() == StatusPedido.CANCELAMENTO_SOLICITADO
                ? "Cancelamento solicitado"
                : "Pedido cancelado";

        String mensagem = event.statusNovo() == StatusPedido.CANCELAMENTO_SOLICITADO
                ? "Sua solicitação de cancelamento do pedido #" + event.idPedido() + " foi registrada."
                : "Seu pedido #" + event.idPedido() + " foi cancelado.";

        notificacaoService.criar(
                event.idPedido(),
                titulo,
                mensagem,
                TipoNotificacao.PEDIDO_CANCELADO
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEstornarPedido(PedidoEstornadoEvent event) {
        notificacaoService.criar(
                event.idPedido(),
                "Pedido estornado",
                "O pedido #" + event.idPedido() + " foi estornado.",
                TipoNotificacao.PEDIDO_ESTORNADO
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAplicarCupom(CupomAplicadoEvent event) {
        notificacaoService.criar(
                event.idPedido(),
                "Cupom aplicado",
                "O cupom " + event.codigoCupom() + " foi aplicado ao pedido #" + event.idPedido() + ".",
                TipoNotificacao.CUPOM_APLICADO
        );
    }

}
