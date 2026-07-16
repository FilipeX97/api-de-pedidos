package br.com.api.pedidos.payment.webhook.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "api.webhook.fake")
public record WebhookFakeProperties(
        @NotBlank(message = "O segredo do webhook é obrigatório")
        @Size(
                min = 32,
                message = "O segredo do webhook deve possuir pelo menos 32 caracteres"
        )
        String secret
) {
}
