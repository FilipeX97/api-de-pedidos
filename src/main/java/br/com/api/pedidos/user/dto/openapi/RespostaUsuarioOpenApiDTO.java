package br.com.api.pedidos.user.dto.openapi;

import br.com.api.pedidos.user.dto.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaUsuario",
        description = "Resposta de sucesso contendo os dados de um usuário"
)
public record RespostaUsuarioOpenApiDTO(
        @Schema(
                description = """
                        Indica que a operação foi concluída com sucesso
                        """,
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Usuário retornado pela operação",
                implementation = UsuarioResponseDTO.class
        )
        UsuarioResponseDTO dados,

        @Schema(
                description = "Mensagem correspondente à operação realizada"
        )
        String mensagem
) {
}
