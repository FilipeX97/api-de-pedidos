package br.com.api.pedidos.order.dto;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.state.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
    Long idPedido,
    Long idUsuario,
    LocalDateTime dataCriacao,
    StatusPedido status,
    BigDecimal valorBruto,
    BigDecimal valorDesconto,
    BigDecimal valorFinal,
    String codigoCupom,
    List<ItemPedidoDTO> itens
) {
    public static PedidoResponseDTO from(Pedido pedido) {
        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getUsuario().getId(),
                pedido.getDataCriacao(),
                pedido.getStatus(),
                pedido.getValorBruto(),
                pedido.getValorDesconto(),
                pedido.getValorFinal(),
                pedido.getCupom() == null ? null : pedido.getCupom().getCodigo(),
                pedido.getItens()
                        .stream()
                        .map(ItemPedidoDTO::from)
                        .toList()
        );
    }
}
