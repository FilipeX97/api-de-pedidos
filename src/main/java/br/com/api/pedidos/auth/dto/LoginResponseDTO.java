package br.com.api.pedidos.auth.dto;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken
) {
}
