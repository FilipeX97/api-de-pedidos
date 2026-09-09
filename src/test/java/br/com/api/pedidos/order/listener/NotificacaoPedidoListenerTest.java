package br.com.api.pedidos.order.listener;

import br.com.api.pedidos.messaging.config.RabbitMqNomes;
import br.com.api.pedidos.messaging.dto.PedidoEventoMensagem;
import br.com.api.pedidos.messaging.producer.PedidoEventoProducer;
import br.com.api.pedidos.order.event.PedidoCanceladoEvent;
import br.com.api.pedidos.order.event.PedidoEntregueEvent;
import br.com.api.pedidos.order.event.PedidoEstornadoEvent;
import br.com.api.pedidos.order.event.PedidoEnviadoEvent;
import br.com.api.pedidos.order.event.PedidoPagoEvent;
import br.com.api.pedidos.order.state.StatusPedido;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificacaoPedidoListenerTest {

    @Test
    void devePublicarEventoDePedidoPagoNoRabbitMQ() {
        PedidoEventoProducer producer =
                mock(PedidoEventoProducer.class);

        NotificacaoPedidoListener listener =
                new NotificacaoPedidoListener(producer);

        LocalDateTime dataHora =
                LocalDateTime.of(
                        2026,
                        9,
                        9,
                        10,
                        30
                );

        PedidoPagoEvent event =
                new PedidoPagoEvent(
                        10L,
                        1L,
                        BigDecimal.valueOf(100),
                        dataHora
                );

        listener.aoPagarPedido(event);

        ArgumentCaptor<PedidoEventoMensagem> mensagemCaptor =
                ArgumentCaptor.forClass(
                        PedidoEventoMensagem.class
                );

        verify(producer).publicar(
                mensagemCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(
                        RabbitMqNomes.ROUTING_KEY_PEDIDO_PAGO
                )
        );

        PedidoEventoMensagem mensagem =
                mensagemCaptor.getValue();

        assertNotNull(mensagem.idEvento());
        assertEquals("PEDIDO_PAGO", mensagem.tipoEvento());
        assertEquals(10L, mensagem.idPedido());
        assertEquals(1L, mensagem.idUsuario());
        assertEquals(
                BigDecimal.valueOf(100),
                mensagem.valor()
        );
        assertEquals(
                dataHora,
                mensagem.dataHora()
        );
        assertNull(mensagem.statusNovo());
    }

    @Test
    void devePublicarEventoDePedidoEnviadoNoRabbitMQ() {
        PedidoEventoProducer producer =
                mock(PedidoEventoProducer.class);

        NotificacaoPedidoListener listener =
                new NotificacaoPedidoListener(producer);

        LocalDateTime dataHora =
                LocalDateTime.of(
                        2026,
                        9,
                        9,
                        11,
                        30
                );

        PedidoEnviadoEvent event =
                new PedidoEnviadoEvent(
                        20L,
                        2L,
                        StatusPedido.ENVIADO,
                        dataHora
                );

        listener.aoEnviarPedido(event);

        ArgumentCaptor<PedidoEventoMensagem> mensagemCaptor =
                ArgumentCaptor.forClass(
                        PedidoEventoMensagem.class
                );

        verify(producer).publicar(
                mensagemCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(
                        RabbitMqNomes.ROUTING_KEY_PEDIDO_ENVIADO
                )
        );

        PedidoEventoMensagem mensagem =
                mensagemCaptor.getValue();

        assertNotNull(mensagem.idEvento());
        assertEquals("PEDIDO_ENVIADO", mensagem.tipoEvento());
        assertEquals(20L, mensagem.idPedido());
        assertEquals(2L, mensagem.idUsuario());
        assertNull(mensagem.valor());
        assertEquals(
                dataHora,
                mensagem.dataHora()
        );
        assertEquals(
                StatusPedido.ENVIADO.name(),
                mensagem.statusNovo()
        );
    }

    @Test
    void devePublicarEventoDePedidoEntregueNoRabbitMQ() {
        PedidoEventoProducer producer =
                mock(PedidoEventoProducer.class);

        NotificacaoPedidoListener listener =
                new NotificacaoPedidoListener(producer);

        LocalDateTime dataHora =
                LocalDateTime.of(
                        2026,
                        9,
                        9,
                        12,
                        30
                );

        PedidoEntregueEvent event =
                new PedidoEntregueEvent(
                        30L,
                        3L,
                        BigDecimal.valueOf(250),
                        dataHora
                );

        listener.aoEntregarPedido(event);

        ArgumentCaptor<PedidoEventoMensagem> mensagemCaptor =
                ArgumentCaptor.forClass(
                        PedidoEventoMensagem.class
                );

        verify(producer).publicar(
                mensagemCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(
                        RabbitMqNomes.ROUTING_KEY_PEDIDO_ENTREGUE
                )
        );

        PedidoEventoMensagem mensagem =
                mensagemCaptor.getValue();

        assertNotNull(mensagem.idEvento());
        assertEquals("PEDIDO_ENTREGUE", mensagem.tipoEvento());
        assertEquals(30L, mensagem.idPedido());
        assertEquals(3L, mensagem.idUsuario());
        assertEquals(
                BigDecimal.valueOf(250),
                mensagem.valor()
        );
        assertEquals(
                dataHora,
                mensagem.dataHora()
        );
        assertNull(mensagem.statusNovo());
    }

    @Test
    void devePublicarCancelamentoSolicitadoNoRabbitMQ() {
        PedidoEventoProducer producer =
                mock(PedidoEventoProducer.class);

        NotificacaoPedidoListener listener =
                new NotificacaoPedidoListener(producer);

        LocalDateTime dataHora =
                LocalDateTime.of(
                        2026,
                        9,
                        9,
                        13,
                        30
                );

        PedidoCanceladoEvent event =
                new PedidoCanceladoEvent(
                        40L,
                        4L,
                        StatusPedido.CRIADO,
                        StatusPedido.CANCELAMENTO_SOLICITADO,
                        dataHora
                );

        listener.aoCancelarPedido(event);

        ArgumentCaptor<PedidoEventoMensagem> mensagemCaptor =
                ArgumentCaptor.forClass(
                        PedidoEventoMensagem.class
                );

        verify(producer).publicar(
                mensagemCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(
                        RabbitMqNomes.ROUTING_KEY_PEDIDO_CANCELADO
                )
        );

        PedidoEventoMensagem mensagem =
                mensagemCaptor.getValue();

        assertEquals(
                "PEDIDO_CANCELADO",
                mensagem.tipoEvento()
        );

        assertEquals(
                StatusPedido.CANCELAMENTO_SOLICITADO.name(),
                mensagem.statusNovo()
        );
    }

    @Test
    void devePublicarCancelamentoDefinitivoNoRabbitMQ() {
        PedidoEventoProducer producer =
                mock(PedidoEventoProducer.class);

        NotificacaoPedidoListener listener =
                new NotificacaoPedidoListener(producer);

        LocalDateTime dataHora =
                LocalDateTime.of(
                        2026,
                        9,
                        9,
                        14,
                        30
                );

        PedidoCanceladoEvent event =
                new PedidoCanceladoEvent(
                        50L,
                        5L,
                        StatusPedido.CANCELAMENTO_SOLICITADO,
                        StatusPedido.CANCELADO,
                        dataHora
                );

        listener.aoCancelarPedido(event);

        ArgumentCaptor<PedidoEventoMensagem> mensagemCaptor =
                ArgumentCaptor.forClass(
                        PedidoEventoMensagem.class
                );

        verify(producer).publicar(
                mensagemCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(
                        RabbitMqNomes.ROUTING_KEY_PEDIDO_CANCELADO
                )
        );

        PedidoEventoMensagem mensagem =
                mensagemCaptor.getValue();

        assertEquals(
                "PEDIDO_CANCELADO",
                mensagem.tipoEvento()
        );

        assertEquals(
                StatusPedido.CANCELADO.name(),
                mensagem.statusNovo()
        );
    }

    @Test
    void devePublicarEventoDePedidoEstornadoNoRabbitMQ() {
        PedidoEventoProducer producer =
                mock(PedidoEventoProducer.class);

        NotificacaoPedidoListener listener =
                new NotificacaoPedidoListener(producer);

        LocalDateTime dataHora =
                LocalDateTime.of(
                        2026,
                        9,
                        9,
                        15,
                        30
                );

        PedidoEstornadoEvent event =
                new PedidoEstornadoEvent(
                        60L,
                        6L,
                        BigDecimal.valueOf(500),
                        dataHora
                );

        listener.aoEstornarPedido(event);

        ArgumentCaptor<PedidoEventoMensagem> mensagemCaptor =
                ArgumentCaptor.forClass(
                        PedidoEventoMensagem.class
                );

        verify(producer).publicar(
                mensagemCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(
                        RabbitMqNomes.ROUTING_KEY_PEDIDO_ESTORNADO
                )
        );

        PedidoEventoMensagem mensagem =
                mensagemCaptor.getValue();

        assertNotNull(mensagem.idEvento());
        assertEquals(
                "PEDIDO_ESTORNADO",
                mensagem.tipoEvento()
        );
        assertEquals(60L, mensagem.idPedido());
        assertEquals(6L, mensagem.idUsuario());
        assertEquals(
                BigDecimal.valueOf(500),
                mensagem.valor()
        );
        assertEquals(
                dataHora,
                mensagem.dataHora()
        );
        assertNull(mensagem.statusNovo());
    }
}