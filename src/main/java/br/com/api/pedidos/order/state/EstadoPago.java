package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import org.springframework.stereotype.Component;

@Component
public final class EstadoPago implements EstadoPedido {

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.PAGO;
    }

    @Override
    public StatusPedido enviar(Pedido pedido) {
        return StatusPedido.ENVIADO;
    }

    @Override
    public StatusPedido cancelar(Pedido pedido) {
        return StatusPedido.CANCELAMENTO_SOLICITADO;
    }
}
