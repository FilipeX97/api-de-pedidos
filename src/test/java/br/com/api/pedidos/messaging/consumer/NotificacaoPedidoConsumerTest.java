package br.com.api.pedidos.messaging.consumer;

import br.com.api.pedidos.messaging.dto.PedidoEventoMensagem;
import br.com.api.pedidos.messaging.service.MensagemProcessadaService;
import br.com.api.pedidos.notification.entity.TipoNotificacao;
import br.com.api.pedidos.notification.service.NotificacaoService;
import br.com.api.pedidos.observability.metrics.MetricasRabbitMqService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificacaoPedidoConsumerTest {

    @Test
    void deveCriarNotificacaoQuandoReceberPedidoPago() {
        NotificacaoService notificacaoService =
                mock(NotificacaoService.class);

        MensagemProcessadaService mensagemProcessadaService =
                mock(MensagemProcessadaService.class);

        MetricasRabbitMqService metricasRabbitMqService =
                mock(MetricasRabbitMqService.class);

        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        NotificacaoPedidoConsumer consumer =
                new NotificacaoPedidoConsumer(
                        notificacaoService,
                        mensagemProcessadaService,
                        metricasRabbitMqService,
                        meterRegistry
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

        when(
                mensagemProcessadaService.processar(
                        eq(mensagem.idEvento()),
                        eq(mensagem.tipoEvento()),
                        any(Runnable.class)
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
    void deveCriarNotificacaoDeCancelamentoSolicitado() {
        NotificacaoService notificacaoService =
                mock(NotificacaoService.class);

        MensagemProcessadaService mensagemProcessadaService =
                mock(MensagemProcessadaService.class);

        MetricasRabbitMqService metricasRabbitMqService =
                mock(MetricasRabbitMqService.class);

        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        NotificacaoPedidoConsumer consumer =
                new NotificacaoPedidoConsumer(
                        notificacaoService,
                        mensagemProcessadaService,
                        metricasRabbitMqService,
                        meterRegistry
                );

        PedidoEventoMensagem mensagem =
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "PEDIDO_CANCELADO",
                        20L,
                        2L,
                        null,
                        LocalDateTime.now(),
                        "CANCELAMENTO_SOLICITADO"
                );

        when(
                mensagemProcessadaService.processar(
                        eq(mensagem.idEvento()),
                        eq(mensagem.tipoEvento()),
                        any(Runnable.class)
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
                20L,
                "Cancelamento solicitado",
                "Sua solicitação de cancelamento do pedido #20 foi registrada.",
                TipoNotificacao.PEDIDO_CANCELADO
        );
    }

    @Test
    void deveCriarNotificacaoDeCancelamentoDefinitivo() {
        NotificacaoService notificacaoService =
                mock(NotificacaoService.class);

        MensagemProcessadaService mensagemProcessadaService =
                mock(MensagemProcessadaService.class);

        MetricasRabbitMqService metricasRabbitMqService =
                mock(MetricasRabbitMqService.class);

        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        NotificacaoPedidoConsumer consumer =
                new NotificacaoPedidoConsumer(
                        notificacaoService,
                        mensagemProcessadaService,
                        metricasRabbitMqService,
                        meterRegistry
                );

        PedidoEventoMensagem mensagem =
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "PEDIDO_CANCELADO",
                        30L,
                        3L,
                        null,
                        LocalDateTime.now(),
                        "CANCELADO"
                );

        when(
                mensagemProcessadaService.processar(
                        eq(mensagem.idEvento()),
                        eq(mensagem.tipoEvento()),
                        any(Runnable.class)
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
                30L,
                "Pedido cancelado",
                "Seu pedido #30 foi cancelado.",
                TipoNotificacao.PEDIDO_CANCELADO
        );
    }

    @Test
    void naoDeveCriarNotificacaoQuandoMensagemForDuplicada() {
        NotificacaoService notificacaoService =
                mock(NotificacaoService.class);

        MensagemProcessadaService mensagemProcessadaService =
                mock(MensagemProcessadaService.class);

        MetricasRabbitMqService metricasRabbitMqService =
                mock(MetricasRabbitMqService.class);

        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        NotificacaoPedidoConsumer consumer =
                new NotificacaoPedidoConsumer(
                        notificacaoService,
                        mensagemProcessadaService,
                        metricasRabbitMqService,
                        meterRegistry
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

        when(
                mensagemProcessadaService.processar(
                        eq(mensagem.idEvento()),
                        eq(mensagem.tipoEvento()),
                        any(Runnable.class)
                )
        ).thenReturn(false);

        consumer.receber(mensagem);

        verify(
                notificacaoService,
                never()
        ).criar(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(TipoNotificacao.class)
        );
    }

    @Test
    void deveRejeitarTipoDeEventoNaoSuportado() {
        NotificacaoService notificacaoService =
                mock(NotificacaoService.class);

        MensagemProcessadaService mensagemProcessadaService =
                mock(MensagemProcessadaService.class);

        MetricasRabbitMqService metricasRabbitMqService =
                mock(MetricasRabbitMqService.class);

        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        NotificacaoPedidoConsumer consumer =
                new NotificacaoPedidoConsumer(
                        notificacaoService,
                        mensagemProcessadaService,
                        metricasRabbitMqService,
                        meterRegistry
                );

        PedidoEventoMensagem mensagem =
                new PedidoEventoMensagem(
                        UUID.randomUUID(),
                        "EVENTO_DESCONHECIDO",
                        10L,
                        1L,
                        BigDecimal.valueOf(200),
                        LocalDateTime.now()
                );

        when(
                mensagemProcessadaService.processar(
                        eq(mensagem.idEvento()),
                        eq(mensagem.tipoEvento()),
                        any(Runnable.class)
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