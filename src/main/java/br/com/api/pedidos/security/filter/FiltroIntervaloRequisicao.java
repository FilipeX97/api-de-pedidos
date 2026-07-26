package br.com.api.pedidos.security.filter;

import br.com.api.pedidos.security.ratelimit.RateLimitService;
import br.com.api.pedidos.security.userdetails.UsuarioSecurity;
import br.com.api.pedidos.security.util.RequestUtils;
import br.com.api.pedidos.security.util.SecurityHashUtils;
import br.com.api.pedidos.shared.response.RespostaApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class FiltroIntervaloRequisicao extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public FiltroIntervaloRequisicao(
            RateLimitService rateLimitService,
            ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = RequestUtils.extrairCaminho(request);

        return uri.startsWith("/auth/")
                || uri.startsWith("/h2-console")
                || uri.startsWith("/webhooks/")
                || uri.equals("/swagger-ui.html")
                || uri.startsWith("/swagger-ui/")
                || uri.equals("/v3/api-docs")
                || uri.startsWith("/v3/api-docs/")
                || uri.equals("/actuator")
                || uri.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String ip = RequestUtils.extrairIp(request);
        String userAgent = RequestUtils.extrairUserAgent(request);
        String userAgentHash = SecurityHashUtils.hashUserAgent(userAgent);
        String chave;

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UsuarioSecurity user) {
            chave = "USER:" + user.getUsuario().getId();
        } else {
            chave = "IP:" + ip + ":" + userAgentHash;
        }

        if (!rateLimitService.permitirRequisicao(chave)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            objectMapper.writeValue(
                    response.getWriter(),
                    RespostaApi.erro("Muitas requisições. Aguarde um momento.")
            );

            return;
        }

        filterChain.doFilter(request, response);
    }
}