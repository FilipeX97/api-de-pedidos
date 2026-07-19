package br.com.api.pedidos.notification.dto.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaPaginaNotificacoes",
        description = """
                Resposta de sucesso contendo uma página de notificações
                """
)
public record RespostaPaginaNotificacoesOpenApiDTO(
        @Schema(
                description = """
                        Indica que a consulta foi concluída com sucesso
                        """,
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Página de notificações encontrada",
                implementation = PaginaNotificacoesOpenApiDTO.class
        )
        PaginaNotificacoesOpenApiDTO dados,

        @Schema(
                description = "Mensagem correspondente à consulta realizada"
        )
        String mensagem
) {
}
