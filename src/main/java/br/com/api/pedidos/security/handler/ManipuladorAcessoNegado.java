package br.com.api.pedidos.security.handler;

import br.com.api.pedidos.shared.response.RespostaApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ManipuladorAcessoNegado implements AccessDeniedHandler {

    private static final String MENSAGEM_ACESSO_NEGADO =
            "Acesso negado";

    private final ObjectMapper objectMapper;

    public ManipuladorAcessoNegado(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest requisicao,
            HttpServletResponse resposta,
            AccessDeniedException excecao
    ) throws IOException {
        resposta.setStatus(HttpStatus.FORBIDDEN.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());

        RespostaApi<Void> corpoResposta =
                RespostaApi.erro(
                        MENSAGEM_ACESSO_NEGADO
                );

        objectMapper.writeValue(
                resposta.getOutputStream(),
                corpoResposta
        );
    }
}