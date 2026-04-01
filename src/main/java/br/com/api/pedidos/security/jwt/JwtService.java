package br.com.api.pedidos.security.jwt;

import br.com.api.pedidos.config.TokenProperties;
import br.com.api.pedidos.security.token.TokenService;
import br.com.api.pedidos.security.util.JwtUtils;
import br.com.api.pedidos.user.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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
        String uaHash = DigestUtils.sha256Hex(userAgent);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", usuario.getPerfil().name());
        claims.put("ip", ip);
        claims.put("ua", uaHash);
        claims.put("pwd", usuario.getSenhaAlteradaEm());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + tokenProperties.expiracao())
                )
                .signWith(Keys.hmacShaKeyFor(chave))
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            return !JwtUtils.tokenExpirado(extrairClaims(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public Date extrairExpiracao(String token) {
        return extrairClaims(token).getExpiration();
    }

    public Claims extrairClaims(String token) {
        return JwtUtils.extrairClaims(token, chave);
    }

    public boolean precisaRenovar(String token) {
        Date expiracaoToken = extrairExpiracao(token);

        long tempoRestante =
                expiracaoToken.getTime() - System.currentTimeMillis();

        return tempoRestante < tokenProperties.tempoAntesExpiracaoParaRenovar();
    }

}