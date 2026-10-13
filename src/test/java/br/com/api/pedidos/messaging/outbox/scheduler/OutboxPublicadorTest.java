package br.com.api.pedidos.messaging.outbox.scheduler;

import br.com.api.pedidos.messaging.dto.PedidoEventoKafkaMensagem;
import br.com.api.pedidos.messaging.outbox.entity.OutboxEvento;
import br.com.api.pedidos.messaging.outbox.service.OutboxEventoService;
import br.com.api.pedidos.messaging.producer.PedidoEventoKafkaProducer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxPublicadorTest {

    @Test
    void devePublicarEventoPendenteEMarcarComoPublicado()
            throws Exception {

        OutboxEventoService service =
                mock(OutboxEventoService.class);

        PedidoEventoKafkaProducer producer =
                mock(PedidoEventoKafkaProducer.class);

        OutboxEvento evento =
                new OutboxEvento(
                        UUID.randomUUID(),
                        "PEDIDO_PAGO",
                        "10",
                        "api.pedidos.eventos.v1",
                        "{}"
                );

        PedidoEventoKafkaMensagem mensagem =
                new PedidoEventoKafkaMensagem(
                        evento.getId(),
                        1,
                        "PEDIDO_PAGO",
                        10L,
                        1L,
                        null,
                        java.time.LocalDateTime.now(),
                        "PAGO"
                );

        when(
                service.buscarPendentes()
        ).thenReturn(
                List.of(evento)
        );

        when(
                service.reconstruirMensagem(evento)
        ).thenReturn(mensagem);

        CompletableFuture<
                SendResult<String, PedidoEventoKafkaMensagem>
                > futuro =
                CompletableFuture.completedFuture(
                        null
                );

        when(
                producer.publicar(
                        eq(evento.getTopico()),
                        eq(evento.getChaveEvento()),
                        eq(mensagem)
                )
        ).thenReturn(futuro);

        OutboxPublicador publicador =
                new OutboxPublicador(
                        service,
                        producer
                );

        publicador.publicarPendencias();

        verify(producer).publicar(
                evento.getTopico(),
                evento.getChaveEvento(),
                mensagem
        );

        verify(service).marcarComoPublicado(
                evento.getId()
        );
    }
}
