package br.com.api.pedidos.payment.webhook.document.repository;

import br.com.api.pedidos.payment.webhook.document.entity.RegistroOperacionalWebhookPagamento;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RegistroOperacionalWebhookPagamentoRepository
        extends MongoRepository<RegistroOperacionalWebhookPagamento, String> {
}
