package br.com.api.pedidos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api.security.token")
public record TokenProperties(
        String chaveSecreta,
        String chaveRefreshSecreta,
        long expiracao,
        long expiracaoRefresh,
        long tempoAntesExpiracaoParaRenovar
) {}