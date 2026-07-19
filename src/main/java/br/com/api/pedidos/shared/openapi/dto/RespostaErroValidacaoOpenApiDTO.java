package br.com.api.pedidos.shared.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(
        name = "RespostaErroValidacao",
        description = """
                Estrutura retornada quando um ou mais campos enviados
                não passam pelas validações da API
                """
)
public record RespostaErroValidacaoOpenApiDTO(
        @Schema(
                description = "Indica que a operação falhou",
                example = "false"
        )
        boolean sucesso,

        @Schema(
                description = """
                        Mapa no qual a chave representa o nome do campo
                        inválido e o valor representa a mensagem de validação
                        """
        )
        Map<String, String> dados,

        @Schema(
                description = "Mensagem geral da falha de validação"
        )
        String mensagem
) {
}
