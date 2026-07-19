package br.com.api.pedidos.coupon.dto.openapi;

import br.com.api.pedidos.coupon.dto.CupomResponseDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "PaginaCupons",
        description = "Página contendo cupons e informações de paginação"
)
public record PaginaCuponsOpenApiDTO(
        @ArraySchema(
                arraySchema = @Schema(
                        description = "Cupons encontrados na página atual"
                ),
                schema = @Schema(
                        implementation = CupomResponseDTO.class
                )
        )
        List<CupomResponseDTO> conteudo,

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
                description = "Quantidade total de cupons encontrados",
                example = "42"
        )
        Long totalElementos,

        @Schema(
                description = "Tamanho máximo solicitado para a página",
                example = "20"
        )
        Integer tamanhoPagina,

        @Schema(
                description = "Quantidade de cupons presentes na página atual",
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
