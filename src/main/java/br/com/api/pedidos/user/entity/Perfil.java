package br.com.api.pedidos.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "PerfilUsuario",
        description = """
                Perfil de autorização do usuário.

                USER possui acesso às funcionalidades comuns.
                ADMIN possui acesso às operações administrativas.
                """,
        example = "USER",
        allowableValues = {
                "USER",
                "ADMIN"
        }
)
public enum Perfil {
    ADMIN,
    USER
}
