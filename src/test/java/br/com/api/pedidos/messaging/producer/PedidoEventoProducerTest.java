package br.com.api.pedidos.messaging.producer;

import br.com.api.pedidos.messaging.dto.PedidoEventoMensagem;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PedidoEventoProducerTest {

    @Test
    void devePublicarMensagemNoExchangeComRoutingKeyInformada() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        PedidoEventoProducer producer = new PedidoEventoProducer(rabbitTemplate);

        UUID idEvento = UUID.randomUUID();
        PedidoEventoMensagem mensagem = new PedidoEventoMensagem(
                idEvento,
                "PEDIDO_PAGO",
                10L,
                1L,
                BigDecimal.valueOf(200),
                LocalDateTime.of(
                        2026, 9, 8, 13, 30
                )
        );

        producer.publicar(mensagem, "pedido.pago");

        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> mensagemCaptor = ArgumentCaptor.forClass(Object.class);

        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                mensagemCaptor.capture()
        );

        assertEquals("api.pedidos.events", exchangeCaptor.getValue());
        assertEquals("pedido.pago", routingKeyCaptor.getValue());
        assertSame(mensagem, mensagemCaptor.getValue());
    }

}
