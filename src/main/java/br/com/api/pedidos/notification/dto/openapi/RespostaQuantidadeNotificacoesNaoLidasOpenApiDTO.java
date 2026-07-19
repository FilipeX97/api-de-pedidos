package br.com.api.pedidos.notification.dto.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaQuantidadeNotificacoesNaoLidas",
        description = """
                Resposta contendo a quantidade de notificações não lidas
                do usuário autenticado
                """
)
public record RespostaQuantidadeNotificacoesNaoLidasOpenApiDTO(
        @Schema(
                description = """
                        Indica que a consulta foi concluída com sucesso
                        """,
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = """
                        Quantidade de notificações que ainda não foram lidas
                        """,
                example = "3",
                type = "integer",
                format = "int64"
        )
        Long dados,

        @Schema(
                description = "Mensagem correspondente à consulta realizada"
        )
        String mensagem
) {
}
