package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import org.springframework.stereotype.Component;

@Component
public final class EstadoCancelamentoSolicitado implements EstadoPedido {

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.CANCELAMENTO_SOLICITADO;
    }

    @Override
    public StatusPedido estornar(Pedido pedido) {
        return StatusPedido.ESTORNADO;
    }
}
