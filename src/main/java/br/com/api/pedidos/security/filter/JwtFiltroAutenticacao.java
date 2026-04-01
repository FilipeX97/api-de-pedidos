package br.com.api.pedidos.security.filter;

import br.com.api.pedidos.auth.service.TokenBlacklistService;
import br.com.api.pedidos.security.jwt.JwtService;
import br.com.api.pedidos.security.userdetails.UsuarioSecurity;
import br.com.api.pedidos.security.util.RequestUtils;
import br.com.api.pedidos.security.util.SecurityUtils;
import br.com.api.pedidos.security.util.TokenUtils;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.service.UsuarioAutenticacaoService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFiltroAutenticacao extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UsuarioAutenticacaoService usuarioAutenticacaoService;

    public JwtFiltroAutenticacao(
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService,
            UsuarioAutenticacaoService usuarioAutenticacaoService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.usuarioAutenticacaoService = usuarioAutenticacaoService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = TokenUtils.extrairToken(request);

        if (token == null ||
                !jwtService.validarToken(token) ||
                tokenBlacklistService.tokenBloqueado(token)) {

            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtService.extrairClaimsAccess(token);

        String tokenIp = claims.get("ip", String.class);
        String tokenUa = claims.get("ua", String.class);
        Long tokenPwd = claims.get("pwd", Long.class);

        String requestIp = RequestUtils.extrairIp(request);
        String userAgent = RequestUtils.extrairUserAgent(request);

        SecurityUtils.validarUserAgent(userAgent);

        String userAgentHash = DigestUtils.sha256Hex(userAgent);

        if (!tokenIp.equals(requestIp) || !tokenUa.equals(userAgentHash)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String email = claims.getSubject();

        UsuarioSecurity usuarioSecurity =
                (UsuarioSecurity) usuarioAutenticacaoService.loadUserByUsername(email);

        Usuario usuario = usuarioSecurity.getUsuario();

        if (!usuario.isAtivo()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (!tokenPwd.equals(usuario.getSenhaAlteradaEm())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        usuarioSecurity,
                        null,
                        usuarioSecurity.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (jwtService.precisaRenovar(token)) {
            String novoToken = jwtService.gerarToken(
                    usuario,
                    requestIp,
                    userAgent
            );

            response.setHeader("X-New-Token", novoToken);
        }

        filterChain.doFilter(request, response);
    }
}
