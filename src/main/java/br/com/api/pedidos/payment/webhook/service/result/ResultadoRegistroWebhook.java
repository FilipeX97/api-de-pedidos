package br.com.api.pedidos.payment.webhook.service.result;

import br.com.api.pedidos.payment.webhook.entity.WebhookPagamentoRecebido;

public record ResultadoRegistroWebhook(
        WebhookPagamentoRecebido evento,
        boolean novo) {
    public boolean duplicado() {
        return !novo;
    }
}