package br.com.api.pedidos.security.util;

import br.com.api.pedidos.shared.exception.RegraNegocioException;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static void validarUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank() || userAgent.length() < 10) {
            throw new RegraNegocioException("Requisição inválida");
        }
    }

}
