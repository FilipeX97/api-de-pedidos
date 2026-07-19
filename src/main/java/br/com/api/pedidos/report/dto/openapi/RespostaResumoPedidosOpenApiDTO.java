package br.com.api.pedidos.report.dto.openapi;

import br.com.api.pedidos.report.dto.ResumoPedidosResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RespostaResumoPedidos",
        description = """
                Resposta de sucesso contendo os indicadores consolidados
                do relatório de pedidos
                """
)
public record RespostaResumoPedidosOpenApiDTO(
        @Schema(
                description = """
                        Indica que o relatório foi gerado com sucesso
                        """,
                example = "true"
        )
        boolean sucesso,

        @Schema(
                description = "Indicadores consolidados dos pedidos",
                implementation = ResumoPedidosResponseDTO.class
        )
        ResumoPedidosResponseDTO dados,

        @Schema(
                description = "Mensagem correspondente à geração do relatório"
        )
        String mensagem
) {
}
