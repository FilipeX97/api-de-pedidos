package br.com.api.pedidos.shared.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaSucessoSemDados",
        description = """
                Estrutura de sucesso utilizada quando a operação
                não retorna conteúdo no campo dados
                """
)
public record RespostaSucessoSemDadosOpenApiDTO(
        @Schema(
                description = "Indica que a operação foi concluída com sucesso",
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Sem conteúdo para esta operação",
                nullable = true
        )
        Object dados,

        @Schema(
                description = "Mensagem correspondente à operação realizada"
        )
        String mensagem
) {
}
