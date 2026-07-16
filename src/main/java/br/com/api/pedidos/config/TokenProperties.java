package br.com.api.pedidos.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "api.security.token")
public record TokenProperties(
        @NotBlank(message = "A chave secreta JWT é obrigatória")
        @Size(
                min = 64,
                message = "A chave secreta JWT deve possuir pelo menos 64 caracteres"
        )
        String chaveSecreta,

        @Positive(message = "A expiração do access token deve ser maior que zero")
        long expiracao,

        @Positive(message = "A expiração do refresh token deve ser maior que zero")
        long expiracaoRefresh,

        @Positive(message = "O tempo de renovação deve ser maior que zero")
        long tempoAntesExpiracaoParaRenovar
) {}