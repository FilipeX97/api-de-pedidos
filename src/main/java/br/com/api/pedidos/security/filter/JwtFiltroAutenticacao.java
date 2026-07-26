package br.com.api.pedidos.security.filter;

import br.com.api.pedidos.auth.service.TokenBlacklistService;
import br.com.api.pedidos.security.jwt.JwtService;
import br.com.api.pedidos.security.service.TokenRenovacaoService;
import br.com.api.pedidos.security.userdetails.UsuarioSecurity;
import br.com.api.pedidos.security.util.RequestUtils;
import br.com.api.pedidos.security.util.SecurityUtils;
import br.com.api.pedidos.security.util.TokenUtils;
import br.com.api.pedidos.shared.response.RespostaApi;
import br.com.api.pedidos.user.service.UsuarioAutenticacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class JwtFiltroAutenticacao extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UsuarioAutenticacaoService usuarioAutenticacaoService;
    private final TokenRenovacaoService tokenRenovacaoService;
    private final ObjectMapper objectMapper;

    private static final Logger log =
            LoggerFactory.getLogger(JwtFiltroAutenticacao.class);

    private static final Set<String> ROTAS_PUBLICAS_AUTH = Set.of(
            "/auth/login",
            "/auth/refresh",
            "/auth/registrar"
    );

    public JwtFiltroAutenticacao(
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService,
            UsuarioAutenticacaoService usuarioAutenticacaoService,
            TokenRenovacaoService tokenRenovacaoService,
            ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.usuarioAutenticacaoService = usuarioAutenticacaoService;
        this.tokenRenovacaoService = tokenRenovacaoService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = RequestUtils.extrairCaminho(request);

        return ROTAS_PUBLICAS_AUTH.contains(uri)
                || uri.startsWith("/h2-console")
                || uri.startsWith("/webhooks/")
                || uri.equals("/swagger-ui.html")
                || uri.startsWith("/swagger-ui/")
                || uri.equals("/v3/api-docs")
                || uri.startsWith("/v3/api-docs/")
                || uri.equals("/actuator/health")
                || uri.startsWith("/actuator/health/")
                || uri.equals("/actuator/info");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        log.debug("Processando autenticação para URI: {}", uri);

        String token = TokenUtils.extrairToken(request);

        if (token == null) {
            respostaNaoAutorizada(response, "Token não enviado");
            return;
        }

        if (!jwtService.validarToken(token)) {
            respostaNaoAutorizada(response, "Token inválido ou expirado");
            return;
        }

        if (tokenBlacklistService.tokenBloqueado(token)) {
            respostaNaoAutorizada(response, "Token bloqueado. Faça login novamente.");
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
        tokenRenovacaoService.renovarSeNecessario(request, response, usuarioSecurity);

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

    private void respostaNaoAutorizada(
            HttpServletResponse response,
            String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), RespostaApi.erro(mensagem));
    }
}
