package br.com.api.pedidos.product.dto.openapi;

import br.com.api.pedidos.product.dto.ProdutoResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaProduto",
        description = "Resposta de sucesso contendo os dados de um produto"
)
public record RespostaProdutoOpenApiDTO(
        @Schema(
                description = """
                        Indica que a operação foi concluída com sucesso
                        """,
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Produto retornado pela operação",
                implementation = ProdutoResponseDTO.class
        )
        ProdutoResponseDTO dados,

        @Schema(
                description = "Mensagem correspondente à operação realizada"
        )
        String mensagem
) {
}
