package br.com.api.pedidos.shared.idempotency.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class GeradorHashRequest {

    private final ObjectMapper objectMapper;

    public GeradorHashRequest(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String gerarHash(Object request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            return DigestUtils.sha256Hex(json);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash da requisição", e);
        }
    }

}
