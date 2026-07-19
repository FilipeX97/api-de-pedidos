package br.com.api.pedidos.notification.dto;

import br.com.api.pedidos.notification.entity.Notificacao;
import br.com.api.pedidos.notification.entity.TipoNotificacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        name = "NotificacaoResponse",
        description = """
                Dados de uma notificação pertencente ao usuário autenticado
                """
)
public record NotificacaoResponseDTO(
        @Schema(
                description = "Identificador único da notificação",
                example = "100"
        )
        Long id,

        @Schema(
                description = "Identificador do usuário que recebeu a notificação",
                example = "1"
        )
        Long idUsuario,

        @Schema(
                description = """
                        Identificador do pedido relacionado à notificação
                        """,
                example = "10"
        )
        Long idPedido,

        @Schema(
                description = "Título resumido da notificação",
                example = "Pagamento confirmado"
        )
        String titulo,

        @Schema(
                description = "Mensagem detalhada da notificação",
                example = "O pagamento do pedido #10 foi confirmado"
        )
        String mensagem,

        @Schema(
                description = "Evento que originou a notificação",
                example = "PEDIDO_PAGO",
                implementation = TipoNotificacao.class
        )
        TipoNotificacao tipo,

        @Schema(
                description = """
                        Indica se a notificação já foi marcada como lida
                        """,
                example = "false"
        )
        boolean lida,

        @Schema(
                description = "Data e hora de criação da notificação",
                example = "2026-07-19T15:30:00",
                format = "date-time"
        )
        LocalDateTime dataCriacao
) {
    public static NotificacaoResponseDTO from(Notificacao notificacao) {
        return new NotificacaoResponseDTO(
                notificacao.getId(),
                notificacao.getIdUsuario(),
                notificacao.getIdPedido(),
                notificacao.getTitulo(),
                notificacao.getMensagem(),
                notificacao.getTipo(),
                notificacao.isLida(),
                notificacao.getDataCriacao()
        );
    }
}
