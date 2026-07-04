package br.com.api.pedidos.order.listener;

import br.com.api.pedidos.order.event.*;
import br.com.api.pedidos.order.history.service.HistoricoPedidoService;
import br.com.api.pedidos.order.state.StatusPedido;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class HistoricoPedidoListener {

    private final HistoricoPedidoService historicoPedidoService;

    public HistoricoPedidoListener(HistoricoPedidoService historicoPedidoService) {
        this.historicoPedidoService = historicoPedidoService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCriarPedido(PedidoCriadoEvent event) {
        historicoPedidoService.registrar(
                event.idPedido(),
                StatusPedido.CRIADO,
                "Pedido Criado"
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoPagarPedido(PedidoPagoEvent event) {
        historicoPedidoService.registrar(
                event.idPedido(),
                StatusPedido.PAGO,
                "Pedido pago com sucesso"
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEnviarPedido(PedidoEnviadoEvent event) {
        historicoPedidoService.registrar(
                event.idPedido(),
                StatusPedido.ENVIADO,
                "Pedido enviado"
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEntregarPedido(PedidoEntregueEvent event) {
        historicoPedidoService.registrar(
                event.idPedido(),
                StatusPedido.ENTREGUE,
                "Pedido entregue"
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCancelarPedido(PedidoCanceladoEvent event) {
        var descricao = event.statusNovo() == StatusPedido.CANCELAMENTO_SOLICITADO
                ? "Cancelamento solicitado"
                : "Pedido cancelado";

        historicoPedidoService.registrar(
                event.idPedido(),
                event.statusNovo(),
                descricao
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEstornarPedido(PedidoEstornadoEvent event) {
        historicoPedidoService.registrar(
                event.idPedido(),
                StatusPedido.ESTORNADO,
                "Pedido estornado"
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAplicarCupom(CupomAplicadoEvent event) {
        historicoPedidoService.registrar(
                event.idPedido(),
                event.statusPedido(),
                "Cupom aplicado: " + event.codigoCupom()
        );
    }
}
