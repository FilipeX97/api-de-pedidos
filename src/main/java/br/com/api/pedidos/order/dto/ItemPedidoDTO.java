package br.com.api.pedidos.order.dto;

import br.com.api.pedidos.order.entity.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoDTO(
        Long itemPedidoId,
        Long produtoId,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
    public static ItemPedidoDTO from(ItemPedido itemPedido) {
        return new ItemPedidoDTO(
                itemPedido.getId(),
                itemPedido.getProduto().getId(),
                itemPedido.getProduto().getNome(),
                itemPedido.getQuantidade(),
                itemPedido.getPrecoUnitario(),
                itemPedido.getSubtotal()
        );
    }
}
