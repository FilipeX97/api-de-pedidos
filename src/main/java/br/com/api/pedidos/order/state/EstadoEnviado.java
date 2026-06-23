package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import org.springframework.stereotype.Component;

@Component
public final class EstadoEnviado implements EstadoPedido {

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.ENVIADO;
    }

    @Override
    public StatusPedido entregar(Pedido pedido) {
        return StatusPedido.ENTREGUE;
    }
}
