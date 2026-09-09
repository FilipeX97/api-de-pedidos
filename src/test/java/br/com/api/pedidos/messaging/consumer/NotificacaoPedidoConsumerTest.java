package br.com.api.pedidos.messaging.consumer;

import br.com.api.pedidos.messaging.dto.PedidoEventoMensagem;
import br.com.api.pedidos.messaging.service.MensagemProcessadaService;
import br.com.api.pedidos.notification.entity.TipoNotificacao;
import br.com.api.pedidos.notification.service.NotificacaoService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificacaoPedidoConsumerTest {

    @Test
    void deveCriarNotificacaoQuandoReceberPedidoPago() {
        NotificacaoService notificacaoService =
                mock(NotificacaoService.class);

        MensagemProcessadaService mensagemProcessadaService =
                mock(MensagemProcessadaService.class);

        NotificacaoPedidoConsumer consumer =
                new NotificacaoPedidoConsumer(
                        notificacaoService,
                        mensagemProcessadaService
                );

        PedidoEventoMensagem mensagem =
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "PEDIDO_PAGO",
                        10L,
                        1L,
                        BigDecimal.valueOf(200),
                        LocalDateTime.now()
                );

        org.mockito.Mockito.when(
                mensagemProcessadaService.processar(
                        org.mockito.ArgumentMatchers.eq(
                                mensagem.idEvento()
                        ),
                        org.mockito.ArgumentMatchers.eq(
                                mensagem.tipoEvento()
                        ),
                        org.mockito.ArgumentMatchers.any(Runnable.class)
                )
        ).thenAnswer(invocacao -> {
            Runnable processamento =
                    invocacao.getArgument(
                            2,
                            Runnable.class
                    );

            processamento.run();

            return true;
        });

        consumer.receber(mensagem);

        verify(notificacaoService).criar(
                10L,
                "Pagamento confirmado",
                "O pagamento do pedido #10 foi confirmado",
                TipoNotificacao.PEDIDO_PAGO
        );
    }

    @Test
    void naoDeveCriarNotificacaoQuandoMensagemForDuplicada() {
        NotificacaoService notificacaoService =
                mock(NotificacaoService.class);

        MensagemProcessadaService mensagemProcessadaService =
                mock(MensagemProcessadaService.class);

        NotificacaoPedidoConsumer consumer =
                new NotificacaoPedidoConsumer(
                        notificacaoService,
                        mensagemProcessadaService
                );

        PedidoEventoMensagem mensagem =
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "PEDIDO_PAGO",
                        10L,
                        1L,
                        BigDecimal.valueOf(200),
                        LocalDateTime.now()
                );

        org.mockito.Mockito.when(
                mensagemProcessadaService.processar(
                        org.mockito.ArgumentMatchers.eq(
                                mensagem.idEvento()
                        ),
                        org.mockito.ArgumentMatchers.eq(
                                mensagem.tipoEvento()
                        ),
                        org.mockito.ArgumentMatchers.any(Runnable.class)
                )
        ).thenReturn(false);

        consumer.receber(mensagem);

        verify(
                notificacaoService,
                org.mockito.Mockito.never()
        ).criar(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(TipoNotificacao.class)
        );
    }
}