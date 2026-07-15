package br.com.api.pedidos.report.projection;

import java.math.BigDecimal;

public interface ResumoPedidosProjection {
    Long getTotalPedidos();
    Long getTotalPedidosPagos();
    Long getTotalPedidosCancelados();
    Long getTotalPedidosAguardandoPagamento();
    BigDecimal getValorTotalVendido();
}
