package br.com.api.pedidos.order.query.dto;

import br.com.api.pedidos.order.entity.ItemPedido;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.state.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoResumoResponseDTO(
        Long idPedido,
        String nomeCliente,
        String emailCliente,
        StatusPedido status,
        BigDecimal valorBruto,
        BigDecimal valorDesconto,
        BigDecimal valorFinal,
        String codigoCupom,
        LocalDateTime dataCriacao,
        Integer quantidadeItens
) {
    public static PedidoResumoResponseDTO from(Pedido pedido) {
        int quantidadeItens = pedido.getItens()
                .stream()
                .mapToInt(ItemPedido::getQuantidade)
                .sum();

        return new PedidoResumoResponseDTO(
                pedido.getId(),
                pedido.getUsuario().getNome(),
                pedido.getUsuario().getEmail(),
                pedido.getStatus(),
                pedido.getValorBruto(),
                pedido.getValorDesconto(),
                pedido.getValorFinal(),
                pedido.getCupom() == null
                        ? null
                        : pedido.getCupom().getCodigo(),
                pedido.getDataCriacao(),
                quantidadeItens
        );
    }
}
