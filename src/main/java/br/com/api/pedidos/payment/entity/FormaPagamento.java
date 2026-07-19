package br.com.api.pedidos.payment.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "FormaPagamento",
        description = """
                Forma utilizada para processar o pagamento.

                Valores disponíveis:
                PIX, CARTAO_CREDITO e BOLETO.
                """
)
public enum FormaPagamento {
    PIX,
    CARTAO_CREDITO,
    BOLETO
}
