package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import org.springframework.stereotype.Component;

@Component
public final class EstadoCriado implements EstadoPedido {

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.CRIADO;
    }

    @Override
    public StatusPedido pagar(Pedido pedido) {
        validarPedidoComItens(pedido);
        return StatusPedido.PAGO;
    }

    @Override
    public StatusPedido aguardarPagamento(Pedido pedido) {
        validarPedidoComItens(pedido);
        return StatusPedido.AGUARDANDO_PAGAMENTO;
    }

    @Override
    public StatusPedido cancelar(Pedido pedido) {
        return StatusPedido.CANCELADO;
    }

    @Override
    public boolean permiteAlterarItens() {
        return true;
    }

    private void validarPedidoComItens(Pedido pedido) {
        if (pedido.estaVazio()) {
            throw new IllegalStateException(
                    "Não é possível iniciar pagamento de um pedido sem itens."
            );
        }
    }
}
