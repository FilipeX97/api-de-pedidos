package br.com.api.pedidos.messaging.outbox.listener;

import br.com.api.pedidos.messaging.dto.PedidoEventoKafkaMensagem;
import br.com.api.pedidos.messaging.outbox.service.OutboxEventoService;
import br.com.api.pedidos.order.event.PedidoPagoEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaPedidoOutboxListenerTest {

    @Test
    void deveRegistrarEventoDePedidoPagoNaOutbox() {
        OutboxEventoService service =
                mock(OutboxEventoService.class);

        KafkaPedidoOutboxListener listener =
                new KafkaPedidoOutboxListener(service);

        LocalDateTime dataHora =
                LocalDateTime.of(
                        2026,
                        9,
                        24,
                        10,
                        30
                );

        PedidoPagoEvent evento =
                new PedidoPagoEvent(
                        10L,
                        1L,
                        BigDecimal.valueOf(200),
                        dataHora
                );

        listener.aoPagarPedido(evento);

        ArgumentCaptor<PedidoEventoKafkaMensagem> captor =
                ArgumentCaptor.forClass(
                        PedidoEventoKafkaMensagem.class
                );

        verify(service).registrar(
                captor.capture()
        );

        PedidoEventoKafkaMensagem mensagem = captor.getValue();

        assertNotNull(mensagem.idEvento());
        assertEquals(1, mensagem.versao());
        assertEquals(
                "PEDIDO_PAGO",
                mensagem.tipoEvento()
        );
        assertEquals(
                10L,
                mensagem.idPedido()
        );
        assertEquals(
                1L,
                mensagem.idUsuario()
        );
        assertEquals(
                BigDecimal.valueOf(200),
                mensagem.valorFinal()
        );
        assertEquals(
                dataHora,
                mensagem.dataHoraEvento()
        );
        assertEquals(
                "PAGO",
                mensagem.statusPedido()
        );
    }
}
