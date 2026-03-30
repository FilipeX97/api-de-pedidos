package br.com.api.pedidos.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FiltroIntervaloRequisicao extends OncePerRequestFilter {

    private final Map<String, Long> ultimaRequisicao = new ConcurrentHashMap<>();

    private static final long INTERVALO_MINIMO = 1000; // 1 segundos

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        long agora = System.currentTimeMillis();

        Long ultima = ultimaRequisicao.get(ip);

        if(ultima != null && (agora - ultima) < INTERVALO_MINIMO) {
            response.setStatus(429);
            response.getWriter().write("Muitas requisições. Por favor, aguarde um momento.");
            return;
        }

        ultimaRequisicao.put(ip, agora);
        filterChain.doFilter(request, response);
    }

}