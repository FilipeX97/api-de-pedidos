package br.com.api.pedidos.order.history.dto.openapi;

import br.com.api.pedidos.order.history.dto.HistoricoPedidoResponseDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "RespostaListaHistoricoPedido",
        description = """
                Resposta de sucesso contendo o histórico de um pedido
                """
)
public record RespostaListaHistoricoPedidoOpenApiDTO(
        @Schema(
                description = """
                        Indica que a consulta foi concluída com sucesso
                        """,
                example = "true"
        )
        boolean sucesso,

        @ArraySchema(
                arraySchema = @Schema(
                        description = """
                                Registros do histórico ordenados do mais
                                recente para o mais antigo
                                """
                ),
                schema = @Schema(
                        implementation =
                                HistoricoPedidoResponseDTO.class
                )
        )
        List<HistoricoPedidoResponseDTO> dados,

        @Schema(
                description = "Mensagem correspondente à consulta realizada"
        )
        String mensagem
) {
}