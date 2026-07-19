package br.com.api.pedidos.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(
        name = "UsuarioAtualizacaoRequest",
        description = """
                Campos que podem ser alterados em um usuário.

                Como a atualização é parcial, somente os campos enviados
                serão modificados.
                """
)
public record UsuarioAtualizacaoRequest(
        @Schema(
                description = "Novo nome do usuário",
                example = "Maria Souza",
                nullable = true,
                minLength = 1,
                maxLength = 100
        )
        @Size(
                max = 100,
                message = "Nome deve ter no máximo 100 caracteres"
        )
        String nome,

        @Schema(
                description = "Novo e-mail utilizado para autenticação",
                example = "maria.souza@exemplo.com",
                format = "email",
                nullable = true,
                minLength = 1,
                maxLength = 150
        )
        @Email(message = "E-mail inválido")
        @Size(
                max = 150,
                message = "E-mail deve ter no máximo 150 caracteres"
        )
        String email,

        @Schema(
                description = "Nova senha do usuário",
                example = "NovaSenha@123",
                format = "password",
                nullable = true,
                minLength = 6,
                maxLength = 100
        )
        @Size(
                min = 6,
                max = 100,
                message = "Senha deve ter entre 6 e 100 caracteres"
        )
        String senha
) {}