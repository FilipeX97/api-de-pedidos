package br.com.api.pedidos.payment.dto.openapi;

import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "RespostaListaPagamentos",
        description = """
                Resposta de sucesso contendo os pagamentos de um pedido
                """
)
public record RespostaListaPagamentosOpenApiDTO(
        @Schema(
                description = """
                        Indica que a consulta foi concluída com sucesso
                        """,
                example = "true"
        )
        boolean sucesso,

        @ArraySchema(
                arraySchema = @Schema(
                        description = "Pagamentos encontrados para o pedido"
                ),
                schema = @Schema(
                        implementation = PagamentoResponseDTO.class
                )
        )
        List<PagamentoResponseDTO> dados,

        @Schema(
                description = "Mensagem correspondente à consulta realizada"
        )
        String mensagem
) {
}
