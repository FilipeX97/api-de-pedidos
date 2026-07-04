package br.com.api.pedidos.order.listener;

import br.com.api.pedidos.audit.entity.TipoAcao;
import br.com.api.pedidos.audit.service.AuditoriaService;
import br.com.api.pedidos.order.event.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AuditoriaPedidoListener {

    private final AuditoriaService auditoriaService;

    public AuditoriaPedidoListener(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCriarPedido(PedidoCriadoEvent event) {
        auditoriaService.registrar(
                event.idPedido(),
                TipoAcao.PEDIDO_CRIADO,
                "Pedido " + event.idPedido() + " criado para o usuário " + event.idUsuario()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoPagarPedido(PedidoPagoEvent event) {
        auditoriaService.registrar(
                event.idPedido(),
                TipoAcao.PEDIDO_PAGO,
                "Pedido " + event.idPedido() + " pago pelo usuário " + event.idUsuario()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEnviarPedido(PedidoEnviadoEvent event) {
        auditoriaService.registrar(
                event.idPedido(),
                TipoAcao.PEDIDO_ENVIADO,
                "Pedido " + event.idPedido() + " enviado"
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEntregarPedido(PedidoEntregueEvent event) {
        auditoriaService.registrar(
                event.idPedido(),
                TipoAcao.PEDIDO_ENTREGUE,
                "Pedido " + event.idPedido() + " entregue"
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCancelarPedido(PedidoCanceladoEvent event) {
        auditoriaService.registrar(
                event.idPedido(),
                TipoAcao.PEDIDO_CANCELADO,
                "Pedido " + event.idPedido()
                        + " mudou de "
                        + event.statusAnterior()
                        + " para "
                        + event.statusNovo()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEstornarPedido(PedidoEstornadoEvent event) {
        auditoriaService.registrar(
                event.idPedido(),
                TipoAcao.PEDIDO_ESTORNADO,
                "Pedido " + event.idPedido() + " estornado"
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAplicarCupom(CupomAplicadoEvent event) {
        auditoriaService.registrar(
                event.idPedido(),
                TipoAcao.CUPOM_APLICADO,
                "Cupom " + event.codigoCupom() + " aplicado ao pedido " + event.idPedido()
        );
    }

}
