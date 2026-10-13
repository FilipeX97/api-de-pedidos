package br.com.api.pedidos.messaging.outbox.service;

import br.com.api.pedidos.messaging.dto.PedidoEventoKafkaMensagem;
import br.com.api.pedidos.messaging.outbox.entity.OutboxEvento;
import br.com.api.pedidos.messaging.outbox.entity.StatusOutboxEvento;
import br.com.api.pedidos.messaging.outbox.repository.OutboxEventoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class OutboxEventoServiceTest {

    @Test
    void deveRegistrarEventoComoPendente() {
        OutboxEventoRepository repository =
                mock(OutboxEventoRepository.class);

        OutboxEventoService service =
                new OutboxEventoService(
                        repository,
                        new ObjectMapper().findAndRegisterModules(),
                        "api.pedidos.eventos.v1"
                );

        UUID idEvento = UUID.randomUUID();

        PedidoEventoKafkaMensagem mensagem =
                new PedidoEventoKafkaMensagem(
                        idEvento,
                        1,
                        "PEDIDO_PAGO",
                        10L,
                        1L,
                        BigDecimal.valueOf(200),
                        LocalDateTime.now(),
                        "PAGO"
                );

        service.registrar(mensagem);

        ArgumentCaptor<OutboxEvento> captor =
                ArgumentCaptor.forClass(
                        OutboxEvento.class
                );

        verify(repository).save(
                captor.capture()
        );

        OutboxEvento evento =
                captor.getValue();

        assertEquals(
                idEvento,
                evento.getId()
        );

        assertEquals(
                "PEDIDO_PAGO",
                evento.getTipoEvento()
        );

        assertEquals(
                "10",
                evento.getChaveEvento()
        );

        assertEquals(
                "api.pedidos.eventos.v1",
                evento.getTopico()
        );

        assertEquals(
                StatusOutboxEvento.PENDENTE,
                evento.getStatus()
        );

        assertEquals(
                0,
                evento.getTentativas()
        );

        assertNotNull(
                evento.getPayload()
        );
    }

    @Test
    void deveMarcarEventoComoPublicado() {
        OutboxEventoRepository repository =
                mock(OutboxEventoRepository.class);

        OutboxEventoService service =
                new OutboxEventoService(
                        repository,
                        new ObjectMapper().findAndRegisterModules(),
                        "api.pedidos.eventos.v1"
                );

        UUID idEvento = UUID.randomUUID();

        OutboxEvento evento =
                new OutboxEvento(
                        idEvento,
                        "PEDIDO_PAGO",
                        "10",
                        "api.pedidos.eventos.v1",
                        "{}"
                );

        when(
                repository.findById(idEvento)
        ).thenReturn(
                java.util.Optional.of(evento)
        );

        service.marcarComoPublicado(idEvento);

        assertEquals(
                StatusOutboxEvento.PUBLICADO,
                evento.getStatus()
        );

        assertNotNull(
                evento.getDataPublicacao()
        );

        assertNotNull(
                evento.getUltimaTentativa()
        );
    }
}
