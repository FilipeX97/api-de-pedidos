package br.com.api.pedidos.coupon.dto.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaPaginaCupons",
        description = "Resposta de sucesso contendo uma página de cupons"
)
public record RespostaPaginaCuponsOpenApiDTO(
        @Schema(
                description = "Indica que a consulta foi concluída com sucesso",
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Página de cupons encontrada",
                implementation = PaginaCuponsOpenApiDTO.class
        )
        PaginaCuponsOpenApiDTO dados,

        @Schema(
                description = "Mensagem correspondente à consulta realizada"
        )
        String mensagem
) {
}
