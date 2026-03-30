package br.com.api.pedidos.security.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {

    public static String extrairIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    public static String extrairUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

}
