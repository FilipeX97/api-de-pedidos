package br.com.api.pedidos.shared.idempotency.service;

import br.com.api.pedidos.shared.idempotency.entity.IdempotencyKey;
import br.com.api.pedidos.shared.idempotency.repository.IdempotencyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRepository idempotencyRepository,
                              ObjectMapper objectMapper) {
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<String> buscarResposta(String chave) {
        return idempotencyRepository
                .findByChave(chave)
                .map(IdempotencyKey::getRespostaJson);
    }

    public void salvarResposta(String chave, Object resposta) {
        try {
            String json = objectMapper.writeValueAsString(resposta);

            IdempotencyKey key = new IdempotencyKey();
            key.setChave(chave);
            key.setRespostaJson(json);
            key.setCriadaEm(new Date());

            idempotencyRepository.save(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
