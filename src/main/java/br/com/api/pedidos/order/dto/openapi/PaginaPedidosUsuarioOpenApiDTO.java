package br.com.api.pedidos.order.dto.openapi;

import br.com.api.pedidos.order.query.dto.PedidoUsuarioResumoResponseDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "PaginaPedidosUsuario",
        description = """
                Página contendo os pedidos pertencentes ao usuário autenticado
                """
)
public record PaginaPedidosUsuarioOpenApiDTO(
        @ArraySchema(
                arraySchema = @Schema(
                        description = "Pedidos encontrados na página atual"
                ),
                schema = @Schema(
                        implementation = PedidoUsuarioResumoResponseDTO.class
                )
        )
        List<PedidoUsuarioResumoResponseDTO> conteudo,

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
                example = "4"
        )
        Integer totalPaginas,

        @Schema(
                description = "Quantidade total de pedidos encontrados",
                example = "68"
        )
        Long totalElementos,

        @Schema(
                description = "Tamanho máximo solicitado para a página",
                example = "20"
        )
        Integer tamanhoPagina,

        @Schema(
                description = "Quantidade de pedidos presentes na página atual",
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
