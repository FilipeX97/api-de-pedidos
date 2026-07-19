package br.com.api.pedidos.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        name = "ResumoPedidosResponse",
        description = """
                Indicadores consolidados dos pedidos encontrados dentro
                do período informado
                """
)
public record ResumoPedidosResponseDTO(
        @Schema(
                description = """
                        Quantidade total de pedidos encontrados no período,
                        independentemente do status
                        """,
                example = "12",
                minimum = "0"
        )
        Long totalPedidos,

        @Schema(
                description = """
                        Quantidade de pedidos contabilizados como venda.

                        São considerados neste indicador os pedidos com
                        status PAGO, ENVIADO ou ENTREGUE.
                        """,
                example = "8",
                minimum = "0"
        )
        Long totalPedidosPagos,

        @Schema(
                description = """
                        Quantidade de pedidos com status CANCELADO.

                        Pedidos com status ESTORNADO não são incluídos
                        neste indicador.
                        """,
                example = "2",
                minimum = "0"
        )
        Long totalPedidosCancelados,

        @Schema(
                description = """
                        Quantidade de pedidos com status
                        AGUARDANDO_PAGAMENTO
                        """,
                example = "1",
                minimum = "0"
        )
        Long totalPedidosAguardandoPagamento,

        @Schema(
                description = """
                        Soma do valor final dos pedidos contabilizados
                        como venda.

                        São somados somente os pedidos com status PAGO,
                        ENVIADO ou ENTREGUE.
                        """,
                example = "2400.00",
                minimum = "0.00"
        )
        BigDecimal valorTotalVendido,

        @Schema(
                description = """
                        Valor médio dos pedidos contabilizados como venda.

                        O cálculo divide valorTotalVendido por
                        totalPedidosPagos e utiliza duas casas decimais.

                        Quando não existem vendas, o resultado é 0.00.
                        """,
                example = "300.00",
                minimum = "0.00"
        )
        BigDecimal ticketMedio
) {
}
