package br.com.api.pedidos.payment.webhook.document.entity;

public enum StatusRegistroOperacionalWebhook {

    RECEBIDO(false),
    PROCESSADO(true),
    DUPLICADO(true),
    ERRO(true);

    private final boolean finalizado;

    StatusRegistroOperacionalWebhook(boolean finalizado) {
        this.finalizado = finalizado;
    }

    public boolean estaFinalizado() {
        return finalizado;
    }
}
