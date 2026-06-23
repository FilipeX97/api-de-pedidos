package br.com.api.pedidos.order.state;

import org.springframework.stereotype.Component;

@Component
public final class EstadoEstornado implements EstadoPedido {

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.ESTORNADO;
    }
}
