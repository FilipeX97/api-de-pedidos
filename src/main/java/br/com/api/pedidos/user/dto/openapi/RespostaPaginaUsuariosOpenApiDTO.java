package br.com.api.pedidos.user.dto.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaPaginaUsuarios",
        description = "Resposta de sucesso contendo uma página de usuários"
)
public record RespostaPaginaUsuariosOpenApiDTO(
        @Schema(
                description = "Indica que a consulta foi concluída com sucesso",
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Página de usuários encontrada",
                implementation = PaginaUsuariosOpenApiDTO.class
        )
        PaginaUsuariosOpenApiDTO dados,

        @Schema(
                description = "Mensagem descritiva da consulta",
                example = "Usuários encontrados"
        )
        String mensagem
) {
}
