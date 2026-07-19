package br.com.api.pedidos.product.dto.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaPaginaProdutos",
        description = "Resposta de sucesso contendo uma página de produtos"
)
public record RespostaPaginaProdutosOpenApiDTO(
        @Schema(
                description = "Indica que a consulta foi concluída com sucesso",
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Página de produtos encontrada",
                implementation = PaginaProdutosOpenApiDTO.class
        )
        PaginaProdutosOpenApiDTO dados,

        @Schema(
                description = "Mensagem descritiva da consulta",
                example = "Produtos encontrados"
        )
        String mensagem
) {
}
