package br.com.api.pedidos.security.filter;

import br.com.api.pedidos.security.ratelimit.RateLimitService;
import br.com.api.pedidos.security.util.RequestUtils;
import br.com.api.pedidos.security.util.SecurityHashUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
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
        String ip = RequestUtils.extrairIp(request);
        String userAgent = RequestUtils.extrairUserAgent(request);
        String userAgentHash = SecurityHashUtils.hashUserAgent(userAgent);
        String chave = ip + ":" + userAgentHash;

        if (!rateLimitService.permitirRequisicao(chave)) {

            response.setStatus(429);
            response.getWriter().write("Muitas requisições. Aguarde um momento.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}