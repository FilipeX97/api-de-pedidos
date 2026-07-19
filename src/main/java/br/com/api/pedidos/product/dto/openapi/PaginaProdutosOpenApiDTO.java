package br.com.api.pedidos.product.dto.openapi;

import br.com.api.pedidos.product.dto.ProdutoResponseDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "PaginaProdutos",
        description = "Página contendo produtos e informações de paginação"
)
public record PaginaProdutosOpenApiDTO(
        @ArraySchema(
                arraySchema = @Schema(
                        description = "Produtos encontrados na página atual"
                ),
                schema = @Schema(
                        implementation = ProdutoResponseDTO.class
                )
        )
        List<ProdutoResponseDTO> conteudo,

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
                example = "8"
        )
        Integer totalPaginas,

        @Schema(
                description = "Quantidade total de produtos encontrados",
                example = "153"
        )
        Long totalElementos,

        @Schema(
                description = "Tamanho máximo solicitado para a página",
                example = "20"
        )
        Integer tamanhoPagina,

        @Schema(
                description = "Quantidade de produtos presentes nesta página",
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
