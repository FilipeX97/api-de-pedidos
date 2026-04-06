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
        String uri = request.getRequestURI();
        System.out.println(uri);
        if (uri.contains("/auth") || uri.contains("/h2-console")) {
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

        Claims claims = jwtService.extrairClaims(token);

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = claims.getSubject();

        UsuarioSecurity usuarioSecurity =
                (UsuarioSecurity) usuarioAutenticacaoService.loadUserByUsername(email);

        if (!validarContextoToken(request, response, claims, usuarioSecurity)) {
            return;
        }

        autenticarUsuario(request, usuarioSecurity);

        if (jwtService.precisaRenovar(token)) {
            renovarToken(request, response, usuarioSecurity);
        }

        filterChain.doFilter(request, response);
    }

    private boolean validarContextoToken(
            HttpServletRequest request,
            HttpServletResponse response,
            Claims claims,
            UsuarioSecurity usuarioSecurity) throws IOException {
        String tokenIp = claims.get("ip", String.class);
        String tokenUa = claims.get("ua", String.class);
        Long tokenPwd = claims.get("pwd", Long.class);
        Long tokenUserId = claims.get("userId", Long.class);

        String requestIp = RequestUtils.extrairIp(request);
        String userAgent = RequestUtils.extrairUserAgent(request);
        SecurityUtils.validarUserAgent(userAgent);
        String userAgentHash = DigestUtils.sha256Hex(userAgent);

        if (!requestIp.equals(tokenIp) || !userAgentHash.equals(tokenUa)) {
            respostaNaoAutorizada(response, "IP ou UserAgent inválido");
            return false;
        }

        var usuario = usuarioSecurity.getUsuario();

        if (!tokenUserId.equals(usuario.getId())) {
            respostaNaoAutorizada(response, "Token inválido");
            return false;
        }

        if (!usuario.isAtivo()) {
            respostaNaoAutorizada(response, "Usuário desativado");
            return false;
        }

        if (!tokenPwd.equals(usuario.getSenhaAlteradaEm())) {
            respostaNaoAutorizada(response, "Senha alterada");
            return false;
        }

        return true;
    }

    private void autenticarUsuario(
            HttpServletRequest request,
            UsuarioSecurity usuarioSecurity) {
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
    }

    private void renovarToken(
            HttpServletRequest request,
            HttpServletResponse response,
            UsuarioSecurity usuarioSecurity) {
        Usuario usuario = usuarioSecurity.getUsuario();
        String requestIp = RequestUtils.extrairIp(request);
        String userAgent = RequestUtils.extrairUserAgent(request);

        String novoToken = jwtService.gerarToken(
                usuario,
                requestIp,
                userAgent
        );

        response.setHeader("X-New-Token", novoToken);
    }

    private void respostaNaoAutorizada(
            HttpServletResponse response,
            String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"erro\":\"" + mensagem + "\"}"
        );
    }
}
