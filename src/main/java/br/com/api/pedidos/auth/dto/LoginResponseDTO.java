package br.com.api.pedidos.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "LoginResponse",
        description = "Tokens emitidos após autenticação ou renovação"
)
public record LoginResponseDTO(
        @Schema(
                description = """
                        Token JWT utilizado para acessar os endpoints
                        protegidos da API
                        """,
                example = "eyJhbGciOiJIUzI1NiJ9.token-jwt-ficticio"
        )
        String accessToken,

        @Schema(
                description = """
                        Token de longa duração utilizado exclusivamente
                        para solicitar novos tokens
                        """,
                example = "refresh-token-ficticio"
        )
        String refreshToken
) {
}
