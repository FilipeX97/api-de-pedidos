package br.com.api.pedidos.auth.repository;

import br.com.api.pedidos.auth.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    boolean existsByToken(String token);
    void deleteByExpirationBefore(Instant agora);
}
