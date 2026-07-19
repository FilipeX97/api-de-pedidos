package br.com.api.pedidos.auth.dto.openapi;

import br.com.api.pedidos.auth.dto.LoginResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaLogin",
        description = """
                Resposta contendo os tokens emitidos durante
                autenticação ou renovação
                """
)
public record RespostaLoginOpenApiDTO(
        @Schema(
                description = """
                        Indica que a operação foi concluída com sucesso
                        """,
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Tokens emitidos pela operação",
                implementation = LoginResponseDTO.class
        )
        LoginResponseDTO dados,

        @Schema(
                description = "Mensagem correspondente à operação realizada"
        )
        String mensagem
) {
}
