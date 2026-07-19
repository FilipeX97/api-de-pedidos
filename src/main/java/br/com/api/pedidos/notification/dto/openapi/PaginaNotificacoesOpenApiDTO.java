package br.com.api.pedidos.notification.dto.openapi;

import br.com.api.pedidos.notification.dto.NotificacaoResponseDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "PaginaNotificacoes",
        description = """
                Página contendo notificações e informações de paginação
                """
)
public record PaginaNotificacoesOpenApiDTO(
        @ArraySchema(
                arraySchema = @Schema(
                        description = """
                                Notificações encontradas na página atual
                                """
                ),
                schema = @Schema(
                        implementation = NotificacaoResponseDTO.class
                )
        )
        List<NotificacaoResponseDTO> conteudo,

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
                example = "3"
        )
        Integer totalPaginas,

        @Schema(
                description = """
                        Quantidade total de notificações encontradas
                        """,
                example = "48"
        )
        Long totalElementos,

        @Schema(
                description = "Tamanho máximo solicitado para a página",
                example = "20"
        )
        Integer tamanhoPagina,

        @Schema(
                description = """
                        Quantidade de notificações presentes na página atual
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
