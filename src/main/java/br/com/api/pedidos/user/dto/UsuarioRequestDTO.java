package br.com.api.pedidos.user.dto;

public record UsuarioRequestDTO(
        String nome,
        String email,
        String senha
) {}