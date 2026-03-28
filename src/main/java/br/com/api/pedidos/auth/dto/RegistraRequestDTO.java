package br.com.api.pedidos.auth.dto;

public record RegistraRequestDTO(
        String nome,
        String email,
        String senha
) {
}
