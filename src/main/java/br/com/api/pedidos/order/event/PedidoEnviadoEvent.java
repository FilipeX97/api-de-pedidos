package br.com.api.pedidos.order.event;

import br.com.api.pedidos.order.state.StatusPedido;

import java.time.LocalDateTime;

public record PedidoEnviadoEvent(
        Long idPedido,
        Long idUsuario,
        StatusPedido statusNovo,
        LocalDateTime dataHoraEnvio) {
}
