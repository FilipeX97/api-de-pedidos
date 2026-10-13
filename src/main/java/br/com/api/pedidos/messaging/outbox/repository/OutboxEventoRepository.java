package br.com.api.pedidos.messaging.outbox.repository;

import br.com.api.pedidos.messaging.outbox.entity.OutboxEvento;
import br.com.api.pedidos.messaging.outbox.entity.StatusOutboxEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventoRepository extends JpaRepository<OutboxEvento, UUID> {

    List<OutboxEvento> findTop50ByStatusOrderByDataCriacaoAsc(
            StatusOutboxEvento status
    );
}
