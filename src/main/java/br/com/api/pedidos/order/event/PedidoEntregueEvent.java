package br.com.api.pedidos.order.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoEntregueEvent(
        Long idPedido,
        Long idUsuario,
        BigDecimal valorFinal,
        LocalDateTime dataHoraEntrega) {
}
