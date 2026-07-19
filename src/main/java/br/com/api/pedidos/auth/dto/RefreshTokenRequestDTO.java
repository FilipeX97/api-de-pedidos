package br.com.api.pedidos.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "RefreshTokenRequest",
        description = "Dados necessários para renovar a autenticação"
)
public record RefreshTokenRequestDTO(
        @Schema(
                description = "Refresh token válido recebido no login",
                example = "refresh-token-ficticio"
        )
        @NotBlank(message = "Refresh token é obrigatório")
        String refreshToken
) {}