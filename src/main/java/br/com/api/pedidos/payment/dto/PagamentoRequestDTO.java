package br.com.api.pedidos.payment.dto;

import br.com.api.pedidos.payment.entity.FormaPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "PagamentoRequest",
        description = "Dados necessários para iniciar o pagamento de um pedido"
)
public record PagamentoRequestDTO(
        @Schema(
                description = "Forma utilizada para processar o pagamento",
                example = "PIX",
                allowableValues = {
                        "PIX",
                        "CARTAO_CREDITO",
                        "BOLETO"
                },
                requiredMode = Schema.RequiredMode.REQUIRED,
                implementation = FormaPagamento.class
        )
        @NotNull(message = "Forma de pagamento é obrigatória")
        FormaPagamento formaPagamento
) {
}
