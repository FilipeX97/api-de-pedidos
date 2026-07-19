package br.com.api.pedidos.user.dto;

import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(
        name = "UsuarioResponse",
        description = "Dados públicos de um usuário"
)
public record UsuarioResponseDTO(
        @Schema(
                description = "Identificador único do usuário",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Nome completo do usuário",
                example = "Maria da Silva"
        )
        String nome,

        @Schema(
                description = "E-mail utilizado para autenticação",
                example = "maria.silva@exemplo.com",
                format = "email"
        )
        String email,

        @Schema(
                description = "Perfil de autorização do usuário",
                example = "USER",
                implementation = Perfil.class
        )
        Perfil perfil,

        @Schema(
                description = "Data em que o usuário foi cadastrado",
                example = "2026-07-17",
                format = "date"
        )
        LocalDate dataCriacao
) {
    public static UsuarioResponseDTO from(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getDataCriacao()
        );
    }
}
