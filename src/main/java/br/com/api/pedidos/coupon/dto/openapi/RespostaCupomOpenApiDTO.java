package br.com.api.pedidos.coupon.dto.openapi;

import br.com.api.pedidos.coupon.dto.CupomResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaCupom",
        description = "Resposta de sucesso contendo os dados de um cupom"
)
public record RespostaCupomOpenApiDTO(
        @Schema(
                description = "Indica que a operação foi concluída com sucesso",
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Cupom retornado pela operação",
                implementation = CupomResponseDTO.class
        )
        CupomResponseDTO dados,

        @Schema(
                description = "Mensagem correspondente à operação realizada"
        )
        String mensagem
) {
}
