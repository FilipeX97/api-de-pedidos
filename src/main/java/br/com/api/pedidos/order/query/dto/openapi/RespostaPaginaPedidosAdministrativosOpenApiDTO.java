package br.com.api.pedidos.order.query.dto.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaPaginaPedidosAdministrativos",
        description = """
                Resposta de sucesso contendo uma página da consulta
                administrativa de pedidos
                """
)
public record RespostaPaginaPedidosAdministrativosOpenApiDTO(
        @Schema(
                description = """
                        Indica que a consulta foi concluída com sucesso
                        """,
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = """
                        Página de pedidos encontrada pela consulta
                        administrativa
                        """,
                implementation =
                        PaginaPedidosAdministrativosOpenApiDTO.class
        )
        PaginaPedidosAdministrativosOpenApiDTO dados,

        @Schema(
                description = "Mensagem correspondente à consulta realizada"
        )
        String mensagem
) {
}
