package br.com.api.pedidos.user.dto.openapi;

import br.com.api.pedidos.user.dto.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "PaginaUsuarios",
        description = "Página contendo usuários e informações de paginação"
)
public record PaginaUsuariosOpenApiDTO(
        @ArraySchema(
                arraySchema = @Schema(
                        description = "Usuários encontrados na página atual"
                ),
                schema = @Schema(
                        implementation = UsuarioResponseDTO.class
                )
        )
        List<UsuarioResponseDTO> conteudo,

        @Schema(
                description = """
                        Número da página atual.

                        A primeira página possui índice zero.
                        """,
                example = "0"
        )
        Integer paginaAtual,

        @Schema(
                description = "Quantidade total de páginas",
                example = "5"
        )
        Integer totalPaginas,

        @Schema(
                description = "Quantidade total de usuários encontrados",
                example = "93"
        )
        Long totalElementos,

        @Schema(
                description = "Tamanho máximo solicitado para a página",
                example = "20"
        )
        Integer tamanhoPagina,

        @Schema(
                description = "Quantidade de usuários presentes nesta página",
                example = "20"
        )
        Integer quantidadeElementos,

        @Schema(
                description = "Indica se esta é a primeira página",
                example = "true"
        )
        Boolean primeiraPagina,

        @Schema(
                description = "Indica se esta é a última página",
                example = "false"
        )
        Boolean ultimaPagina,

        @Schema(
                description = "Indica se a página não possui conteúdo",
                example = "false"
        )
        Boolean vazia
) {
}
