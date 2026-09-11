package br.com.api.pedidos.integration.messaging;

import br.com.api.pedidos.integration.container.ContainersIntegracao;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static br.com.api.pedidos.messaging.config.RabbitMqNomes.EXCHANGE_DEAD_LETTER;
import static br.com.api.pedidos.messaging.config.RabbitMqNomes.EXCHANGE_EVENTOS;
import static br.com.api.pedidos.messaging.config.RabbitMqNomes.FILA_NOTIFICACOES_PEDIDO;
import static br.com.api.pedidos.messaging.config.RabbitMqNomes.FILA_NOTIFICACOES_PEDIDO_DLQ;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class RabbitMqTopologyIT extends ContainersIntegracao {

    @Test
    void deveCriarTopologiaDoRabbitMq()
            throws IOException, TimeoutException {

        ConnectionFactory factory =
                new ConnectionFactory();

        factory.setHost(
                RABBITMQ.getHost()
        );

        factory.setPort(
                RABBITMQ.getAmqpPort()
        );

        factory.setUsername(
                RABBITMQ.getAdminUsername()
        );

        factory.setPassword(
                RABBITMQ.getAdminPassword()
        );

        factory.setVirtualHost(
                "/"
        );

        try (Connection connection =
                     factory.newConnection()) {

            var channel =
                    connection.createChannel();

            assertDoesNotThrow(
                    () -> channel.exchangeDeclarePassive(
                            EXCHANGE_EVENTOS
                    )
            );

            assertDoesNotThrow(
                    () -> channel.exchangeDeclarePassive(
                            EXCHANGE_DEAD_LETTER
                    )
            );

            assertDoesNotThrow(
                    () -> channel.queueDeclarePassive(
                            FILA_NOTIFICACOES_PEDIDO
                    )
            );

            assertDoesNotThrow(
                    () -> channel.queueDeclarePassive(
                            FILA_NOTIFICACOES_PEDIDO_DLQ
                    )
            );

            channel.close();
        }
    }
}