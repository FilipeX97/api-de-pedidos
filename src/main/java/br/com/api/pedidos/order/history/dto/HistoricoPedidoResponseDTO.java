package br.com.api.pedidos.order.history.dto;

import br.com.api.pedidos.order.history.entity.HistoricoPedido;
import br.com.api.pedidos.order.state.StatusPedido;

import java.time.LocalDateTime;

public record HistoricoPedidoResponseDTO(
        Long id,
        Long idPedido,
        StatusPedido status,
        String descricao,
        LocalDateTime dataCriacao) {
    public static HistoricoPedidoResponseDTO from(HistoricoPedido historico) {
        return new HistoricoPedidoResponseDTO(
                historico.getId(),
                historico.getPedido().getId(),
                historico.getStatus(),
                historico.getDescricao(),
                historico.getDataCriacao()
        );
    }
}
