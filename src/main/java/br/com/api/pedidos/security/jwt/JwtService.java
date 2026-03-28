package br.com.api.pedidos.security.jwt;

import br.com.api.pedidos.user.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${api.security.token.secret}")
    private String chaveSecreta;

    @Value("${api.security.token.refresh-secret}")
    private String chaveRefreshSecreta;

    public String gerarToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .claim("role", usuario.getPerfil().name())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 3600000)
                )
                .signWith(Keys.hmacShaKeyFor(chaveSecreta.getBytes()))
                .compact();
    }

    public String gerarRefreshToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 604800000)
                )
                .signWith(Keys.hmacShaKeyFor(chaveRefreshSecreta.getBytes()))
                .compact();
    }

    public String gerarTokenUsuarioEmailPerfil(String email, String perfil) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", perfil)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 3600000)
                )
                .signWith(Keys.hmacShaKeyFor(chaveSecreta.getBytes()))
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(chaveSecreta.getBytes()))
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
                    .setSigningKey(Keys.hmacShaKeyFor(chaveRefreshSecreta.getBytes()))
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
        Date expiracao = extrairExpiracao(token);

        long tempoRestante =
                expiracao.getTime() - System.currentTimeMillis();

        long cincoMinutos = 5 * 60 * 1000;
        return tempoRestante < cincoMinutos;
    }

    private Claims extrairClaimsAccess(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(chaveSecreta.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Claims extrairClaimsRefresh(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(chaveRefreshSecreta.getBytes()))
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