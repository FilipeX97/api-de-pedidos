package br.com.api.pedidos.order.dto.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaPaginaPedidosUsuario",
        description = """
                Resposta de sucesso contendo uma página de pedidos
                do usuário autenticado
                """
)
public record RespostaPaginaPedidosUsuarioOpenApiDTO(
        @Schema(
                description = "Indica que a consulta foi concluída com sucesso",
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Página de pedidos encontrada",
                implementation = PaginaPedidosUsuarioOpenApiDTO.class
        )
        PaginaPedidosUsuarioOpenApiDTO dados,

        @Schema(
                description = "Mensagem correspondente à consulta realizada"
        )
        String mensagem
) {
}