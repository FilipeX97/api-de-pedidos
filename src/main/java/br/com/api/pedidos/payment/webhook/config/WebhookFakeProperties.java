package br.com.api.pedidos.payment.webhook.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api.webhook.fake")
public record WebhookFakeProperties(
        String secret) {
}
