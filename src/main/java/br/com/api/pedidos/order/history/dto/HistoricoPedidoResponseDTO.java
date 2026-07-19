package br.com.api.pedidos.order.history.dto;

import br.com.api.pedidos.order.history.entity.HistoricoPedido;
import br.com.api.pedidos.order.state.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        name = "HistoricoPedidoResponse",
        description = """
                Registro de uma alteração ocorrida durante o ciclo
                de vida de um pedido
                """
)
public record HistoricoPedidoResponseDTO(
        @Schema(
                description = "Identificador único do registro de histórico",
                example = "25"
        )
        Long id,

        @Schema(
                description = "Identificador do pedido relacionado ao histórico",
                example = "10"
        )
        Long idPedido,

        @Schema(
                description = """
                        Status do pedido no momento em que o registro
                        foi criado
                        """,
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
                description = "Descrição do evento registrado no histórico",
                example = "Pedido pago com sucesso"
        )
        String descricao,

        @Schema(
                description = "Data e hora em que o histórico foi registrado",
                example = "2026-07-19T15:45:00",
                format = "date-time"
        )
        LocalDateTime dataCriacao
) {
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
