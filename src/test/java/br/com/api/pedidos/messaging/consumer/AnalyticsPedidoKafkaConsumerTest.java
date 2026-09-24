package br.com.api.pedidos.messaging.consumer;

import br.com.api.pedidos.messaging.dto.PedidoEventoKafkaMensagem;
import br.com.api.pedidos.messaging.service.MensagemKafkaProcessadaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalyticsPedidoKafkaConsumerTest {

    @Test
    void deveReceberEventoKafkaValido() {
        MensagemKafkaProcessadaService service =
                mock(MensagemKafkaProcessadaService.class);

        AnalyticsPedidoKafkaConsumer consumer =
                new AnalyticsPedidoKafkaConsumer(service);

        PedidoEventoKafkaMensagem mensagem =
                mensagemValida();

        when(
                service.processar(
                        eq(mensagem.idEvento()),
                        any(String.class),
                        any(Runnable.class)
                )
        ).thenReturn(true);

        assertDoesNotThrow(
                () -> consumer.receber(
                        mensagem,
                        criarRegistro(mensagem)
                )
        );
    }

    @Test
    void deveRejeitarMensagemSemIdPedido() {
        MensagemKafkaProcessadaService service =
                mock(MensagemKafkaProcessadaService.class);

        AnalyticsPedidoKafkaConsumer consumer =
                new AnalyticsPedidoKafkaConsumer(service);

        PedidoEventoKafkaMensagem mensagem =
                new PedidoEventoKafkaMensagem(
                        UUID.randomUUID(),
                        1,
                        "PEDIDO_PAGO",
                        null,
                        1L,
                        BigDecimal.valueOf(100),
                        LocalDateTime.now(),
                        "PAGO"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> consumer.receber(
                        mensagem,
                        criarRegistro(mensagem)
                )
        );
    }

    private PedidoEventoKafkaMensagem mensagemValida() {
        return new PedidoEventoKafkaMensagem(
                UUID.randomUUID(),
                1,
                "PEDIDO_PAGO",
                10L,
                1L,
                BigDecimal.valueOf(100),
                LocalDateTime.now(),
                "PAGO"
        );
    }

    private ConsumerRecord<String, PedidoEventoKafkaMensagem> criarRegistro(
            PedidoEventoKafkaMensagem mensagem
    ) {
        return new ConsumerRecord<>(
                "api.pedidos.eventos.v1",
                0,
                10L,
                "10",
                mensagem
        );
    }
}