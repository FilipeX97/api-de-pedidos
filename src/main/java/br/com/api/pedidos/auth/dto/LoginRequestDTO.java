package br.com.api.pedidos.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "LoginRequest",
        description = "Credenciais utilizadas para autenticar um usuário"
)
public record LoginRequestDTO(
        @Schema(
                description = "E-mail cadastrado do usuário",
                example = "usuario@exemplo.com",
                format = "email",
                minLength = 1,
                maxLength = 150
        )
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @Schema(
                description = "Senha do usuário",
                example = "Senha@123",
                format = "password",
                minLength = 6,
                maxLength = 100
        )
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
        String senha
) {
}
