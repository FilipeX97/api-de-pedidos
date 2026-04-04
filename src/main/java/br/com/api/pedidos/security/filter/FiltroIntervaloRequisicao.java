package br.com.api.pedidos.security.filter;

import br.com.api.pedidos.security.ratelimit.RateLimitService;
import br.com.api.pedidos.security.userdetails.UsuarioSecurity;
import br.com.api.pedidos.security.util.RequestUtils;
import br.com.api.pedidos.security.util.SecurityHashUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FiltroIntervaloRequisicao extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public FiltroIntervaloRequisicao(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (uri.startsWith("/h2-console") || uri.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

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
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"erro\":\"Muitas requisições. Aguarde um momento.\"}"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }
}