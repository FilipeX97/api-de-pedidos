package br.com.api.pedidos.payment.webhook.repository;

import br.com.api.pedidos.payment.webhook.entity.WebhookPagamentoRecebido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookPagamentoRecebidoRepository
        extends JpaRepository<WebhookPagamentoRecebido, Long> {
    Optional<WebhookPagamentoRecebido> findByEventId(String eventId);
}
