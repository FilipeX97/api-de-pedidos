package br.com.api.pedidos.security.jwt;

import br.com.api.pedidos.config.TokenProperties;
import br.com.api.pedidos.security.token.TokenService;
import br.com.api.pedidos.security.util.JwtUtils;
import br.com.api.pedidos.security.util.SecurityHashUtils;
import br.com.api.pedidos.user.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService implements TokenService {

    private final TokenProperties tokenProperties;
    private final byte[] chave;

    public JwtService(TokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
        chave = tokenProperties.chaveSecreta().getBytes(StandardCharsets.UTF_8);
    }

    public String gerarToken(Usuario usuario, String ip, String userAgent) {
        String uaHash = SecurityHashUtils.hashUserAgent(userAgent);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", usuario.getPerfil().name());
        claims.put("ip", ip);
        claims.put("ua", uaHash);
        claims.put("pwd", usuario.getSenhaAlteradaEm());
        claims.put("userId", usuario.getId());

        Instant agora = Instant.now();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getEmail())
                .setIssuedAt(Date.from(agora))
                .setExpiration(Date.from(agora.plusMillis(tokenProperties.expiracao())))
                .signWith(Keys.hmacShaKeyFor(chave))
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            Claims claims = extrairClaims(token);

            if (claims.getSubject() == null ||
                    claims.get("userId") == null ||
                    claims.get("ip") == null ||
                    claims.get("ua") == null) {
                return false;
            }

            return !JwtUtils.tokenExpirado(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public Instant extrairExpiracao(String token) {
        return extrairClaims(token).getExpiration().toInstant();
    }

    public String extrairUserId(String token) {
        return String.valueOf(extrairClaims(token).get("userId"));
    }

    public Claims extrairClaims(String token) {
        return JwtUtils.extrairClaims(token, chave);
    }

    public boolean precisaRenovar(String token) {
        Instant expiracaoToken = extrairExpiracao(token);

        long tempoRestante =
                expiracaoToken.toEpochMilli() - System.currentTimeMillis();

        return tempoRestante < tokenProperties.tempoAntesExpiracaoParaRenovar();
    }

}