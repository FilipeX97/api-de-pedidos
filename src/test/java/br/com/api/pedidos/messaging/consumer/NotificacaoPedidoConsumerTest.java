package br.com.api.pedidos.messaging.consumer;

import br.com.api.pedidos.messaging.dto.PedidoEventoMensagem;
import br.com.api.pedidos.notification.entity.TipoNotificacao;
import br.com.api.pedidos.notification.service.NotificacaoService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificacaoPedidoConsumerTest {

    @Test
    void deveCriarNotificacaoQuandoReceberPedidoPago() {
        NotificacaoService notificacaoService = mock(NotificacaoService.class);

        NotificacaoPedidoConsumer consumer =
                new NotificacaoPedidoConsumer(notificacaoService);

        PedidoEventoMensagem mensagem =
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "PEDIDO_PAGO",
                        10L,
                        1L,
                        BigDecimal.valueOf(200),
                        LocalDateTime.now()
                );

        consumer.receber(mensagem);

        verify(notificacaoService).criar(
                10L,
                "Pagamento confirmado",
                "O pagamento do pedido #10 foi confirmado",
                TipoNotificacao.PEDIDO_PAGO
        );
    }

    @Test
    void deveRejeitarTipoDeEventoNaoSuportado() {
        NotificacaoService notificacaoService = mock(NotificacaoService.class);

        NotificacaoPedidoConsumer consumer =
                new NotificacaoPedidoConsumer(notificacaoService);

        PedidoEventoMensagem mensagem =
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "EVENTO_DESCONHECIDO",
                        10L,
                        1L,
                        BigDecimal.valueOf(200),
                        LocalDateTime.now()
                );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> consumer.receber(mensagem)
                );

        assertEquals(
                "Tipo de evento de pedido não suportado: EVENTO_DESCONHECIDO",
                excecao.getMessage()
        );
    }
}
