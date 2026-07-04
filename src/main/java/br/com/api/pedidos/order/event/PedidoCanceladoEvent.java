package br.com.api.pedidos.order.event;

import br.com.api.pedidos.order.state.StatusPedido;

import java.time.LocalDateTime;

public record PedidoCanceladoEvent(
        Long idPedido,
        Long idUsuario,
        StatusPedido statusAnterior,
        StatusPedido statusNovo,
        LocalDateTime dataHoraCancelamento) {
}
