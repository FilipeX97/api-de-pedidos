package br.com.api.pedidos.security.util;

import org.apache.commons.codec.digest.DigestUtils;

public final class SecurityHashUtils {

    private SecurityHashUtils() {}

    public static String hashUserAgent(String userAgent) {
        return DigestUtils.sha256Hex(userAgent);
    }

}
