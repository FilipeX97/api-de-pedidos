package br.com.api.pedidos.security.jwt;

import br.com.api.pedidos.config.TokenProperties;
import br.com.api.pedidos.security.token.TokenService;
import br.com.api.pedidos.user.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService implements TokenService {

    private final TokenProperties tokenProperties;

    public JwtService(TokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
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
                .signWith(Keys.hmacShaKeyFor(tokenProperties.chaveSecreta().getBytes()))
                .compact();
    }

    public String gerarRefreshToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + tokenProperties.expiracaoRefresh())
                )
                .signWith(Keys.hmacShaKeyFor(tokenProperties.chaveRefreshSecreta().getBytes()))
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(tokenProperties.chaveSecreta().getBytes()))
                    .build()
                    .parseClaimsJws(token);

            return !tokenExpiradoAccess(token);

        } catch (Exception e) {
            return false;
        }
    }

    public boolean validarTokenRefresh(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(tokenProperties.chaveRefreshSecreta().getBytes()))
                    .build()
                    .parseClaimsJws(token);

            return !tokenExpiradoRefresh(token);

        } catch (Exception e) {
            return false;
        }
    }

    public String extrairEmail(String token) {
        return extrairClaimsAccess(token).getSubject();
    }

    public String extrairEmailRefresh(String token) {
        return extrairClaimsRefresh(token).getSubject();
    }

    public String extrairPerfil(String token) {
        return extrairClaimsAccess(token).get("role", String.class);
    }

    public Date extrairExpiracao(String token) {
        return extrairClaimsAccess(token)
                .getExpiration();
    }

    public String extrairIp(String token) {
        return extrairClaimsAccess(token).get("ip", String.class);
    }

    public String extrairUserAgent(String token) {
        return extrairClaimsAccess(token).get("ua", String.class);
    }

    public boolean precisaRenovar(String token) {
        Date expiracaoToken = extrairExpiracao(token);

        long tempoRestante =
                expiracaoToken.getTime() - System.currentTimeMillis();

        return tempoRestante < tokenProperties.tempoAntesExpiracaoParaRenovar();
    }

    public Claims extrairClaimsAccess(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(tokenProperties.chaveSecreta().getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Claims extrairClaimsRefresh(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(tokenProperties.chaveRefreshSecreta().getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean tokenExpiradoAccess(String token) {
        return extrairClaimsAccess(token)
                .getExpiration()
                .before(new Date());
    }

    private boolean tokenExpiradoRefresh(String token) {
        return extrairClaimsRefresh(token)
                .getExpiration()
                .before(new Date());
    }
}