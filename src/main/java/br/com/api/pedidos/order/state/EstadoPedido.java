package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;

public interface EstadoPedido {

    StatusPedido getStatus();

    default StatusPedido pagar(Pedido pedido) {
        throw new IllegalStateException(
                "Pedido com status " + getStatus() + " não pode ser pago."
        );
    }

    default StatusPedido enviar(Pedido pedido) {
        throw new IllegalStateException(
                "Pedido com status " + getStatus() + " não pode ser enviado."
        );
    }

    default StatusPedido entregar(Pedido pedido) {
        throw new IllegalStateException(
                "Pedido com status " + getStatus() + " não pode ser entregue."
        );
    }

    default StatusPedido cancelar(Pedido pedido) {
        throw new IllegalStateException(
                "Pedido com status " + getStatus() + " não pode ser cancelado."
        );
    }

    default StatusPedido estornar(Pedido pedido) {
        throw new IllegalStateException(
                "Pedido com status " + getStatus() + " não pode ser estornado."
        );
    }

    default boolean permiteAlterarItens() {
        return false;
    }

    default boolean permiteAplicarCupom() {
        return permiteAlterarItens();
    }
}
