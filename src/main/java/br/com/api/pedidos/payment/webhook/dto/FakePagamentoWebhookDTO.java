package br.com.api.pedidos.payment.webhook.dto;

import br.com.api.pedidos.payment.entity.StatusPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(
        name = "FakePagamentoWebhook",
        description = """
                Evento simulado enviado pelo gateway fake para comunicar
                uma alteração no status de uma transação.

                O corpo completo da requisição é utilizado no cálculo
                e na validação da assinatura HMAC-SHA256.
                """
)
public record FakePagamentoWebhookDTO(
        @Schema(
                description = """
                        Identificador único do evento no gateway.

                        O campo é utilizado para impedir que o mesmo evento
                        seja processado mais de uma vez.
                        """,
                example = "evt-pagamento-550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "EventId do webhook é obrigatório")
        String eventId,

        @Schema(
                description = """
                        Tipo do evento enviado pelo gateway.

                        O único valor aceito atualmente é PAYMENT_UPDATED.
                        """,
                example = "PAYMENT_UPDATED",
                allowableValues = {
                        "PAYMENT_UPDATED"
                },
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Tipo do evento é obrigatório")
        String tipo,

        @Schema(
                description = """
                        Código da transação retornado durante a criação
                        do pagamento
                        """,
                example = "PIX-550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Código da transação é obrigatório")
        String codigoTransacao,

        @Schema(
                description = """
                        Novo status informado pelo gateway.

                        Somente PENDENTE, APROVADO e RECUSADO podem ser
                        enviados por este webhook.
                        """,
                example = "APROVADO",
                allowableValues = {
                        "PENDENTE",
                        "APROVADO",
                        "RECUSADO"
                },
                implementation = StatusPagamento.class,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Status do pagamento é obrigatório")
        StatusPagamento statusPagamento,

        @Schema(
                description = """
                        Data informativa enviada pelo gateway.

                        O campo é opcional e atualmente não participa
                        das regras de processamento do webhook.
                        """,
                example = "2026-07-19T18:30:00Z",
                nullable = true
        )
        LocalDateTime dataEvento
) {
}
