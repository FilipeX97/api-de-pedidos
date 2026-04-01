package br.com.api.pedidos.security.util;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static void validarUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank() || userAgent.length() < 10) {
            throw new RuntimeException("Requisição inválida");
        }
    }

}
