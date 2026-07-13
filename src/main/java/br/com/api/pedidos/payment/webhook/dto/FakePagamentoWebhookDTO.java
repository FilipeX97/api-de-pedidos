package br.com.api.pedidos.payment.webhook.dto;

import br.com.api.pedidos.payment.entity.StatusPagamento;

public record FakePagamentoWebhookDTO(
        String eventId,
        String tipo,
        String codigoTransacao,
        StatusPagamento statusPagamento,
        String dataEvento) {
}
