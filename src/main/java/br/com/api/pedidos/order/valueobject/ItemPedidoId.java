package br.com.api.pedidos.order.valueobject;

public record ItemPedidoId(Long valor) {

    public ItemPedidoId {
        if(valor == null || valor <=0){
            throw new IllegalArgumentException("Id do item do pedido inválido");
        }
    }
}
