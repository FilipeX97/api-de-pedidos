package br.com.api.pedidos.integration.messaging;

import br.com.api.pedidos.integration.container.ContainersIntegracao;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class RabbitMqContextIT extends ContainersIntegracao {

    @Test
    void deveSubirAplicacaoComRabbitMq() {
        assertTrue(
                RABBITMQ.isRunning()
        );
    }
}