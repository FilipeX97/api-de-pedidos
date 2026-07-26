package br.com.api.pedidos.observability.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String CHAVE_MDC_REQUEST_ID = "requestId";
    public static final Pattern REQUEST_ID_VALIDO =
            Pattern.compile("^[A-Za-z0-9._-]{1,100}$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = obterOuGerarRequestId(request);
        response.setHeader(HEADER_REQUEST_ID, requestId);
        request.setAttribute(HEADER_REQUEST_ID, requestId);
        MDC.put(CHAVE_MDC_REQUEST_ID, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CHAVE_MDC_REQUEST_ID);
        }
    }

    private String obterOuGerarRequestId(HttpServletRequest request) {
        String requestIdRecebido = request.getHeader(HEADER_REQUEST_ID);

        if (requestIdRecebido != null
                && REQUEST_ID_VALIDO
                .matcher(requestIdRecebido)
                .matches()) {
            return requestIdRecebido;
        }

        return UUID.randomUUID().toString();
    }
}
