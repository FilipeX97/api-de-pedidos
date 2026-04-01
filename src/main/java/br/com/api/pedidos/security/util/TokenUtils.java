package br.com.api.pedidos.security.util;

import jakarta.servlet.http.HttpServletRequest;

public final class TokenUtils {

    private TokenUtils() {}

    public static String extrairToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        return header.substring(7);
    }

}
