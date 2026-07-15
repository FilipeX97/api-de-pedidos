package br.com.api.pedidos.report.dto;

import java.math.BigDecimal;

public record ResumoPedidosResponseDTO(
        Long totalPedidos,
        Long totalPedidosPagos,
        Long totalPedidosCancelados,
        Long totalPedidosAguardandoPagamento,
        BigDecimal valorTotalVendido,
        BigDecimal ticketMedio
) {
}
