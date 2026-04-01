package br.com.api.pedidos.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

public class JwtUtils {

    private JwtUtils() {
    }

    public static Claims extrairClaims(String token, byte[] chaveSecreta) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(chaveSecreta))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean tokenExpirado(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
