package br.com.api.pedidos.user.dto;

public record UsuarioUpdateDTO(
        String nome,
        String email,
        String senha
) {
}
