package br.com.api.pedidos.integration.messaging;

import br.com.api.pedidos.integration.container.ContainersIntegracao;
import br.com.api.pedidos.messaging.config.RabbitMqNomes;
import br.com.api.pedidos.messaging.dto.PedidoEventoMensagem;
import br.com.api.pedidos.messaging.producer.PedidoEventoProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class RabbitMqProducerIT extends ContainersIntegracao {

    @Autowired
    private PedidoEventoProducer producer;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitListenerEndpointRegistry listenerRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void devePublicarMensagemNoExchangeERoteaLaParaFila()
            throws Exception {

        String nomeFila =
                "teste-producer-" + UUID.randomUUID();

        Queue fila =
                new Queue(
                        nomeFila,
                        false,
                        true,
                        true
                );

        rabbitAdmin.declareQueue(fila);

        rabbitAdmin.declareBinding(
                new Binding(
                        nomeFila,
                        Binding.DestinationType.QUEUE,
                        RabbitMqNomes.EXCHANGE_EVENTOS,
                        RabbitMqNomes.ROUTING_KEY_PEDIDO_PAGO,
                        null
                )
        );

        listenerRegistry.stop();

        try {
            PedidoEventoMensagem mensagem =
                    new PedidoEventoMensagem(
                            UUID.randomUUID(),
                            "PEDIDO_PAGO",
                            10L,
                            1L,
                            BigDecimal.valueOf(200),
                            LocalDateTime.now()
                    );

            producer.publicar(
                    mensagem,
                    RabbitMqNomes.ROUTING_KEY_PEDIDO_PAGO
            );

            Message mensagemRecebida =
                    rabbitTemplate.receive(nomeFila, 5000);

            assertNotNull(mensagemRecebida);

            PedidoEventoMensagem mensagemConvertida =
                    objectMapper.readValue(
                            mensagemRecebida.getBody(),
                            PedidoEventoMensagem.class
                    );

            assertEquals(mensagem.idEvento(), mensagemConvertida.idEvento());
            assertEquals(mensagem.tipoEvento(), mensagemConvertida.tipoEvento());
            assertEquals(mensagem.idPedido(), mensagemConvertida.idPedido());
            assertEquals(mensagem.idUsuario(), mensagemConvertida.idUsuario());
            assertEquals(mensagem.valor(), mensagemConvertida.valor());

        } finally {
            rabbitAdmin.deleteQueue(nomeFila);
            listenerRegistry.start();
        }
    }
}