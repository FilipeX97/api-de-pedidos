package br.com.api.pedidos.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "UsuarioCriacaoRequest",
        description = """
                Dados obrigatórios para o cadastro administrativo
                de um novo usuário
                """
)
public record UsuarioCriacaoRequestDTO(
        @Schema(
                description = "Nome completo do usuário",
                example = "Maria da Silva",
                minLength = 1,
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Nome é obrigatório")
        @Size(
                max = 100,
                message = "Nome deve ter no máximo 100 caracteres"
        )
        String nome,

        @Schema(
                description = "E-mail que será utilizado para autenticação",
                example = "maria.silva@exemplo.com",
                format = "email",
                minLength = 1,
                maxLength = 150,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(
                max = 150,
                message = "E-mail deve ter no máximo 150 caracteres"
        )
        String email,

        @Schema(
                description = "Senha inicial do usuário",
                example = "Senha@123",
                format = "password",
                minLength = 6,
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Senha é obrigatória")
        @Size(
                min = 6,
                max = 100,
                message = "Senha deve ter entre 6 e 100 caracteres"
        )
        String senha
) {
}
