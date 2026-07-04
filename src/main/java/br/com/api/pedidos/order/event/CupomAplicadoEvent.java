package br.com.api.pedidos.order.event;

import br.com.api.pedidos.order.state.StatusPedido;

import java.time.LocalDateTime;

public record CupomAplicadoEvent(
        Long idPedido,
        Long idUsuario,
        String codigoCupom,
        StatusPedido statusPedido,
        LocalDateTime dataHoraAplicacao) {
}
