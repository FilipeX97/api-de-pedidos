package br.com.api.pedidos.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "RegistroUsuarioRequest",
        description = "Dados necessários para registrar um novo usuário"
)
public record RegistraRequestDTO(
        @Schema(
                description = "Nome completo do usuário",
                example = "João da Silva",
                minLength = 1,
                maxLength = 100
        )
        @NotBlank(message = "Nome é obrigatório")
        @Size(
                min = 1,
                max = 100,
                message = "Nome deve ter entre 1 e 100 caracteres"
        )
        String nome,

        @Schema(
                description = "E-mail que será usado no login",
                example = "joao.silva@exemplo.com",
                format = "email",
                minLength = 1,
                maxLength = 150
        )
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(
                min = 1,
                max = 150,
                message = "E-mail deve ter no máximo 150 caracteres"
        )
        String email,

        @Schema(
                description = "Senha de acesso do usuário",
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
