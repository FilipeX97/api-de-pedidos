package br.com.api.pedidos.payment.webhook.dto;

import br.com.api.pedidos.payment.entity.StatusPagamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FakePagamentoWebhookDTO(
        @NotBlank(message = "EventId do webhook é obrigatório")
        String eventId,

        @NotBlank(message = "Tipo do evento é obrigatório")
        String tipo,

        @NotBlank(message = "Código da transação é obrigatório")
        String codigoTransacao,

        @NotNull(message = "Status do pagamento é obrigatório")
        StatusPagamento statusPagamento,

        String dataEvento
) {
}
