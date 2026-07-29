package br.com.api.pedidos.payment.webhook.document.dto.openapi;

import br.com.api.pedidos.payment.webhook.document.dto
        .RegistroOperacionalWebhookPagamentoResponseDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "PaginaRegistrosOperacionaisWebhook",
        description = """
                Página contendo registros operacionais de webhooks e
                informações de paginação
                """
)
public record PaginaRegistrosOperacionaisWebhookOpenApiDTO(
        @ArraySchema(
                arraySchema = @Schema(
                        description = """
                                Registros operacionais encontrados na
                                página atual
                                """
                ),
                schema = @Schema(
                        implementation =
                                RegistroOperacionalWebhookPagamentoResponseDTO
                                        .class
                )
        )
        List<RegistroOperacionalWebhookPagamentoResponseDTO> conteudo,

        @Schema(
                description = """
                        Número da página atual.

                        A primeira página possui índice zero.
                        """,
                example = "0"
        )
        Integer paginaAtual,

        @Schema(
                description = "Quantidade total de páginas",
                example = "5"
        )
        Integer totalPaginas,

        @Schema(
                description = """
                        Quantidade total de registros que correspondem
                        aos filtros
                        """,
                example = "82"
        )
        Long totalElementos,

        @Schema(
                description = "Tamanho solicitado para a página",
                example = "20"
        )
        Integer tamanhoPagina,

        @Schema(
                description = """
                        Quantidade de registros presentes na página atual
                        """,
                example = "20"
        )
        Integer quantidadeElementos,

        @Schema(
                description = "Indica se esta é a primeira página",
                example = "true"
        )
        Boolean primeiraPagina,

        @Schema(
                description = "Indica se esta é a última página",
                example = "false"
        )
        Boolean ultimaPagina,

        @Schema(
                description = "Indica se a página não possui conteúdo",
                example = "false"
        )
        Boolean vazia
) {
}