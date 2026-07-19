package br.com.api.pedidos.order.dto.openapi;

import br.com.api.pedidos.order.dto.PedidoResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaPedido",
        description = "Resposta de sucesso contendo os dados detalhados de um pedido"
)
public record RespostaPedidoOpenApiDTO(
        @Schema(
                description = "Indica que a operação foi concluída com sucesso",
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Dados detalhados do pedido",
                implementation = PedidoResponseDTO.class
        )
        PedidoResponseDTO dados,

        @Schema(
                description = "Mensagem correspondente à operação realizada"
        )
        String mensagem
) {
}
