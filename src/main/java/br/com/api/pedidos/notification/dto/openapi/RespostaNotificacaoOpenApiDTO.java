package br.com.api.pedidos.notification.dto.openapi;

import br.com.api.pedidos.notification.dto.NotificacaoResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaNotificacao",
        description = """
                Resposta de sucesso contendo os dados de uma notificação
                """
)
public record RespostaNotificacaoOpenApiDTO(
        @Schema(
                description = """
                        Indica que a operação foi concluída com sucesso
                        """,
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Notificação retornada pela operação",
                implementation = NotificacaoResponseDTO.class
        )
        NotificacaoResponseDTO dados,

        @Schema(
                description = "Mensagem correspondente à operação realizada"
        )
        String mensagem
) {
}
