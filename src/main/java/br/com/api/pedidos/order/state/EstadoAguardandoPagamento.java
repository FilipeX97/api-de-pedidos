package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import org.springframework.stereotype.Component;

@Component
public final class EstadoAguardandoPagamento implements EstadoPedido {

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.AGUARDANDO_PAGAMENTO;
    }

    @Override
    public StatusPedido confirmarPagamento(Pedido pedido) {
        return StatusPedido.PAGO;
    }

    @Override
    public StatusPedido cancelar(Pedido pedido) {
        return StatusPedido.CANCELADO;
    }
}
