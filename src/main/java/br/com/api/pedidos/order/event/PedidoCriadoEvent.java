package br.com.api.pedidos.order.event;

import java.time.LocalDateTime;

public record PedidoCriadoEvent(
        Long idPedido,
        Long idUsuario,
        LocalDateTime dataHora) {
}
