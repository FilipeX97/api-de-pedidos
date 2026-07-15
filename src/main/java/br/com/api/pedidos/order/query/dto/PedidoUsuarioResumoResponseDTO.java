package br.com.api.pedidos.order.query.dto;

import br.com.api.pedidos.order.entity.ItemPedido;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.state.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoUsuarioResumoResponseDTO(
        Long idPedido,
        LocalDateTime dataCriacao,
        StatusPedido status,
        BigDecimal valorBruto,
        BigDecimal valorDesconto,
        BigDecimal valorFinal,
        String codigoCupom,
        Integer quantidadeItens
) {
    public static PedidoUsuarioResumoResponseDTO from(
            Pedido pedido
    ) {
        int quantidadeItens = pedido.getItens()
                .stream()
                .mapToInt(ItemPedido::getQuantidade)
                .sum();

        return new PedidoUsuarioResumoResponseDTO(
                pedido.getId(),
                pedido.getDataCriacao(),
                pedido.getStatus(),
                pedido.getValorBruto(),
                pedido.getValorDesconto(),
                pedido.getValorFinal(),
                pedido.getCupom() == null
                        ? null
                        : pedido.getCupom().getCodigo(),
                quantidadeItens
        );
    }
}
