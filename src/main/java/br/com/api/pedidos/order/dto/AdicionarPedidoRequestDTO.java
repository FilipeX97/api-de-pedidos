package br.com.api.pedidos.order.dto;

public record AdicionarPedidoRequestDTO(
        Long idProduto,
        Integer quantidade
) {
}
