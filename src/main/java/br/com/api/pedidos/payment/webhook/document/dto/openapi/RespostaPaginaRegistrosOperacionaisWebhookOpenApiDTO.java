package br.com.api.pedidos.payment.webhook.document.dto.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaPaginaRegistrosOperacionaisWebhook",
        description = """
                Resposta de sucesso contendo uma página de registros
                operacionais de webhooks de pagamento
                """
)
public record RespostaPaginaRegistrosOperacionaisWebhookOpenApiDTO(
        @Schema(
                description = """
                        Indica que a consulta foi concluída com sucesso
                        """,
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = """
                        Página de registros operacionais encontrada pela
                        consulta administrativa
                        """,
                implementation =
                        PaginaRegistrosOperacionaisWebhookOpenApiDTO.class
        )
        PaginaRegistrosOperacionaisWebhookOpenApiDTO dados,

        @Schema(
                description = "Mensagem correspondente à consulta",
                example = "Registros operacionais de webhooks encontrados"
        )
        String mensagem
) {
}