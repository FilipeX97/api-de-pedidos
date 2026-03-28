package br.com.api.pedidos.auth.dto;

public record LoginRequestDTO(
        String email,
        String senha
) {
}
