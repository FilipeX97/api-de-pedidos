package br.com.api.pedidos.shared.idempotency.job;

import br.com.api.pedidos.shared.idempotency.repository.IdempotencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LimpezaChavesIdempotencyJob {

    private static final Logger log =
            LoggerFactory.getLogger(LimpezaChavesIdempotencyJob.class);

    private final IdempotencyRepository repository;

    public LimpezaChavesIdempotencyJob(IdempotencyRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void limparChavesExpiradas() {
        try {
            repository.deleteByExpiraEmBefore(Instant.now());
        } catch (Exception e) {
            log.error("Erro ao limpar chaves de idempotencia expiradas", e);
        }
    }

}
