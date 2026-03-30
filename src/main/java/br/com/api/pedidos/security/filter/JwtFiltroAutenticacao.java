package br.com.api.pedidos.security.filter;

import br.com.api.pedidos.auth.service.TokenBlacklistService;
import br.com.api.pedidos.security.jwt.JwtService;
import br.com.api.pedidos.security.util.RequestUtils;
import br.com.api.pedidos.security.util.UtilToken;
import br.com.api.pedidos.user.service.UsuarioService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFiltroAutenticacao extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UsuarioService usuarioService;

    public JwtFiltroAutenticacao(
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService,
            UsuarioService usuarioService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.usuarioService = usuarioService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = UtilToken.extrairToken(request);

        if (token == null ||
                !jwtService.validarToken(token) ||
                tokenBlacklistService.tokenBloqueado(token)) {

            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtService.extrairClaimsAccess(token);

        String tokenIp = claims.get("ip", String.class);
        String tokenUa = claims.get("ua", String.class);

        String requestIp = RequestUtils.extrairIp(request);
        String userAgent = RequestUtils.extrairUserAgent(request);

        if (userAgent == null || userAgent.isBlank() || userAgent.length() < 10) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String userAgentHash = DigestUtils.sha256Hex(userAgent);

        if (!tokenIp.equals(requestIp) || !tokenUa.equals(userAgentHash)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String email = claims.getSubject();
        String perfil = claims.get("role", String.class);

        UserDetails usuario = usuarioService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        usuario.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (jwtService.precisaRenovar(token)) {
            String novoToken = jwtService.gerarTokenUsuarioEmailPerfil(
                    email,
                    perfil,
                    requestIp,
                    userAgentHash
            );

            response.setHeader("X-New-Token", novoToken);
        }

        filterChain.doFilter(request, response);
    }
}
