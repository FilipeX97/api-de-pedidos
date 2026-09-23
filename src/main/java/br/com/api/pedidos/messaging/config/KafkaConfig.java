package br.com.api.pedidos.messaging.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class KafkaConfig {

    @Bean
    public NewTopic topicoEventosPedidos(
            @Value("${app.kafka.topico-pedidos:"
                + KafkaNomes.TOPICO_EVENTOS_PEDIDOS + "}")
            String nomeTopico
    ) {
        return new NewTopic(
                nomeTopico,
                KafkaNomes.NUMERO_PARTICOES,
                KafkaNomes.FATOR_REPLICACAO
        );
    }
}
