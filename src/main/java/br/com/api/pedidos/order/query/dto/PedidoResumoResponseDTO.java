package br.com.api.pedidos.order.query.dto;

import br.com.api.pedidos.order.entity.ItemPedido;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.state.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(
        name = "PedidoResumoAdministrativo",
        description = """
                Resumo administrativo de um pedido encontrado na consulta
                paginada
                """
)
public record PedidoResumoResponseDTO(
        @Schema(
                description = "Identificador único do pedido",
                example = "150"
        )
        Long idPedido,

        @Schema(
                description = "Nome do cliente proprietário do pedido",
                example = "João da Silva"
        )
        String nomeCliente,

        @Schema(
                description = "E-mail do cliente proprietário do pedido",
                example = "joao.silva@exemplo.com",
                format = "email"
        )
        String emailCliente,

        @Schema(
                description = "Status atual do pedido",
                example = "PAGO",
                allowableValues = {
                        "CRIADO",
                        "AGUARDANDO_PAGAMENTO",
                        "PAGO",
                        "ENVIADO",
                        "ENTREGUE",
                        "CANCELAMENTO_SOLICITADO",
                        "ESTORNADO",
                        "CANCELADO"
                },
                implementation = StatusPedido.class
        )
        StatusPedido status,

        @Schema(
                description = """
                        Valor total dos itens antes da aplicação dos descontos
                        """,
                example = "500.00"
        )
        BigDecimal valorBruto,

        @Schema(
                description = "Valor total de desconto aplicado ao pedido",
                example = "75.00"
        )
        BigDecimal valorDesconto,

        @Schema(
                description = """
                        Valor final do pedido após a aplicação dos descontos
                        """,
                example = "425.00"
        )
        BigDecimal valorFinal,

        @Schema(
                description = """
                        Código do cupom aplicado ao pedido.

                        O campo será nulo quando o pedido não possuir cupom.
                        """,
                example = "DESCONTO15",
                nullable = true
        )
        String codigoCupom,

        @Schema(
                description = "Data e hora de criação do pedido",
                example = "2026-07-19T14:30:00",
                format = "date-time"
        )
        LocalDateTime dataCriacao,

        @Schema(
                description = """
                        Soma das quantidades de todos os itens do pedido
                        """,
                example = "3",
                minimum = "0"
        )
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
