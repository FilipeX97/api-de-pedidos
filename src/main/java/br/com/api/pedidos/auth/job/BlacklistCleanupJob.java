package br.com.api.pedidos.auth.job;

import br.com.api.pedidos.auth.repository.BlacklistedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BlacklistCleanupJob {

    private final BlacklistedTokenRepository repository;

    public BlacklistCleanupJob(BlacklistedTokenRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 * * * *") // a cada hora
    public void limparTokensExpirados() {
        repository.deleteByExpirationBefore(Instant.now());
    }

}
