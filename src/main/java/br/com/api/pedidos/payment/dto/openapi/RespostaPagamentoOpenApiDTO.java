package br.com.api.pedidos.payment.dto.openapi;

import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaPagamento",
        description = """
                Resposta de sucesso contendo os dados de um pagamento
                """
)
public record RespostaPagamentoOpenApiDTO(
        @Schema(
                description = """
                        Indica que a operação foi concluída com sucesso
                        """,
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Pagamento retornado pela operação",
                implementation = PagamentoResponseDTO.class
        )
        PagamentoResponseDTO dados,

        @Schema(
                description = "Mensagem correspondente à operação realizada"
        )
        String mensagem
) {
}
