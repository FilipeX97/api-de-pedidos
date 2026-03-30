package br.com.api.pedidos.shared.idempotency.repository;

import br.com.api.pedidos.shared.idempotency.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository
        extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByChave(String chave);
}
