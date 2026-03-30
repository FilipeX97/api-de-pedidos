package br.com.api.pedidos.security.jwt;

import br.com.api.pedidos.config.TokenProperties;
import br.com.api.pedidos.user.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final TokenProperties tokenProperties;

    public JwtService(TokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
    }

    public String gerarToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .claim("role", usuario.getPerfil().name())
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

    public String gerarTokenUsuarioEmailPerfil(String email, String perfil) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", perfil)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + tokenProperties.expiracao())
                )
                .signWith(Keys.hmacShaKeyFor(tokenProperties.chaveSecreta().getBytes()))
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

    public boolean precisaRenovar(String token) {
        Date expiracaoToken = extrairExpiracao(token);

        long tempoRestante =
                expiracaoToken.getTime() - System.currentTimeMillis();

        return tempoRestante < tokenProperties.tempoAntesExpiracaoParaRenovar();
    }

    private Claims extrairClaimsAccess(String token) {
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