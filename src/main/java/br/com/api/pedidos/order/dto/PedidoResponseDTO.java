package br.com.api.pedidos.order.dto;

import br.com.api.pedidos.order.entity.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
    Long idPedido,
    Long idUsuario,
    LocalDateTime dataCriacao,
    BigDecimal valorTotal,
    List<ItemPedidoDTO> itens
) {
    public static PedidoResponseDTO from(Pedido pedido) {
        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getUsuario().getId(),
                pedido.getDataCriacao(),
                pedido.getValorTotal(),
                pedido.getItens()
                        .stream()
                        .map(ItemPedidoDTO::from)
                        .toList()
        );
    }
}
