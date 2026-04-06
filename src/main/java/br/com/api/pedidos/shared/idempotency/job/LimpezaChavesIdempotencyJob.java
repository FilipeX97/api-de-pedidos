package br.com.api.pedidos.shared.idempotency.job;

import br.com.api.pedidos.shared.idempotency.repository.IdempotencyRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LimpezaChavesIdempotencyJob {

    private final IdempotencyRepository repository;

    public LimpezaChavesIdempotencyJob(IdempotencyRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void limparChavesExpiradas() {
        try {
            repository.deleteByExpiraEmBefore(Instant.now());
        } catch (Exception e) {
            e.printStackTrace(); // ou log.error(...)
        }
    }

}
