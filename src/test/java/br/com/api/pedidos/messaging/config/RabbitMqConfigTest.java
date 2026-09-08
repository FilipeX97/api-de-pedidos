package br.com.api.pedidos.messaging.config;

import br.com.api.pedidos.messaging.dto.PedidoEventoMensagem;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.core.MessageProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RabbitMqConfigTest {

    @Test
    void deveConverterMensagemParaJson() {
        MessageConverter converter =
                new RabbitMqConfig().conversaoDeMensagemDoRabbitMq();

        PedidoEventoMensagem mensagem =
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "PEDIDO_PAGO",
                        10L,
                        1L,
                        BigDecimal.valueOf(200),
                        LocalDateTime.of(
                                2026, 9, 8, 13, 30
                        )
                );

        Message message = converter.toMessage(mensagem, new MessageProperties());

        String json =
                new String(
                        message.getBody(),
                        java.nio.charset.StandardCharsets.UTF_8
                );

        assertTrue(json.contains("\"tipoEvento\":\"PEDIDO_PAGO\""));
        assertTrue(json.contains("\"idPedido\":10"));
        assertTrue(json.contains("\"idUsuario\":1"));
        assertTrue(json.contains("\"valor\":200"));

        assertNotNull(message.getMessageProperties().getContentType());
    }

}
