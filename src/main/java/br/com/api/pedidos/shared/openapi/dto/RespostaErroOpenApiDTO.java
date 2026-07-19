package br.com.api.pedidos.shared.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaErro",
        description = "Estrutura padrão retornada quando uma operação falha"
)
public record RespostaErroOpenApiDTO(
        @Schema(
                description = "Indica que a operação falhou",
                example = "false"
        )
        boolean sucesso,

        @Schema(
                description = """
                        Conteúdo adicional do erro.

                        Normalmente é nulo para erros que não são de validação.
                        """,
                nullable = true
        )
        Object dados,

        @Schema(
                description = "Mensagem correspondente ao erro ocorrido"
        )
        String mensagem
) {
}
