package br.com.api.pedidos.config;

import br.com.api.pedidos.payment.webhook.document.repository.RegistroOperacionalWebhookPagamentoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.mockito.Mockito.mock;

@Configuration(proxyBeanMethods = false)
@Profile("test")
public class MongoTesteConfig {

    @Bean
    public RegistroOperacionalWebhookPagamentoRepository
    registroOperacionalWebhookPagamentoRepository() {
        return mock(
                RegistroOperacionalWebhookPagamentoRepository.class
        );
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return mock(MongoTemplate.class);
    }
}