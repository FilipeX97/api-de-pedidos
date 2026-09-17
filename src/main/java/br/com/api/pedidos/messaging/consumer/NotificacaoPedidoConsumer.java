package br.com.api.pedidos.messaging.consumer;

import br.com.api.pedidos.messaging.config.RabbitMqNomes;
import br.com.api.pedidos.messaging.dto.PedidoEventoMensagem;
import br.com.api.pedidos.messaging.service.MensagemProcessadaService;
import br.com.api.pedidos.notification.entity.TipoNotificacao;
import br.com.api.pedidos.notification.service.NotificacaoService;
import br.com.api.pedidos.observability.metrics.MetricasRabbitMqService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoPedidoConsumer {

    private final NotificacaoService notificacaoService;
    private final MensagemProcessadaService mensagemProcessadaService;
    private final MetricasRabbitMqService metricasRabbitMqService;
    private final MeterRegistry meterRegistry;

    public NotificacaoPedidoConsumer(
            NotificacaoService notificacaoService,
            MensagemProcessadaService mensagemProcessadaService,
            MetricasRabbitMqService metricasRabbitMqService,
            MeterRegistry meterRegistry
    ) {
        this.notificacaoService = notificacaoService;
        this.mensagemProcessadaService = mensagemProcessadaService;
        this.metricasRabbitMqService = metricasRabbitMqService;
        this.meterRegistry = meterRegistry;
    }

    @RabbitListener(
            queues = RabbitMqNomes.FILA_NOTIFICACOES_PEDIDO
    )
    public void receber(PedidoEventoMensagem mensagem) {
        Timer.Sample amostra =
                metricasRabbitMqService
                        .iniciarProcessamento(meterRegistry);

        try {

            boolean processada =
                    mensagemProcessadaService.processar(
                            mensagem.idEvento(),
                            mensagem.tipoEvento(),
                            () -> processarMensagem(mensagem)
                    );

            if (processada) {
                metricasRabbitMqService.registrarMensagemProcessada();
            } else {
                metricasRabbitMqService.registrarMensagemDuplicada();
            }
        } catch (RuntimeException e) {
            metricasRabbitMqService.registrarErroProcessamento();
            throw e;
        } finally {
            metricasRabbitMqService.finalizarProcessamento(amostra);
        }
    }

    private void processarMensagem(PedidoEventoMensagem mensagem) {
        switch (mensagem.tipoEvento()) {
            case "PEDIDO_PAGO" -> processarPedidoPago(mensagem);
            case "PEDIDO_ENVIADO" -> processarPedidoEnviado(mensagem);
            case "PEDIDO_ENTREGUE" -> processarPedidoEntregue(mensagem);
            case "PEDIDO_CANCELADO" -> processarPedidoCancelado(mensagem);
            case "PEDIDO_ESTORNADO" -> processarPedidoEstornado(mensagem);
            default -> throw new IllegalArgumentException(
                    "Tipo de evento de pedido não suportado: "
                            + mensagem.tipoEvento()
            );
        }
    }

    private void processarPedidoPago(PedidoEventoMensagem mensagem) {
        notificacaoService.criar(
                mensagem.idPedido(),
                "Pagamento confirmado",
                "O pagamento do pedido #"
                        + mensagem.idPedido()
                        + " foi confirmado",
                TipoNotificacao.PEDIDO_PAGO
        );
    }

    private void processarPedidoEnviado(
            PedidoEventoMensagem mensagem
    ) {
        notificacaoService.criar(
                mensagem.idPedido(),
                "Pedido enviado",
                "Seu pedido #"
                        + mensagem.idPedido()
                        + " foi enviado.",
                TipoNotificacao.PEDIDO_ENVIADO
        );
    }

    private void processarPedidoEntregue(
            PedidoEventoMensagem mensagem
    ) {
        notificacaoService.criar(
                mensagem.idPedido(),
                "Pedido entregue",
                "Seu pedido #"
                        + mensagem.idPedido()
                        + " foi entregue.",
                TipoNotificacao.PEDIDO_ENTREGUE
        );
    }

    private void processarPedidoCancelado(
            PedidoEventoMensagem mensagem
    ) {
        boolean cancelamentoSolicitado =
                "CANCELAMENTO_SOLICITADO".equals(
                        mensagem.statusNovo()
                );

        String titulo = cancelamentoSolicitado
                ? "Cancelamento solicitado"
                : "Pedido cancelado";

        String texto = cancelamentoSolicitado
                ? "Sua solicitação de cancelamento do pedido #"
                + mensagem.idPedido()
                + " foi registrada."
                : "Seu pedido #"
                + mensagem.idPedido()
                + " foi cancelado.";

        notificacaoService.criar(
                mensagem.idPedido(),
                titulo,
                texto,
                TipoNotificacao.PEDIDO_CANCELADO
        );
    }

    private void processarPedidoEstornado(
            PedidoEventoMensagem mensagem
    ) {
        notificacaoService.criar(
                mensagem.idPedido(),
                "Pedido estornado",
                "O pedido #"
                        + mensagem.idPedido()
                        + " foi estornado.",
                TipoNotificacao.PEDIDO_ESTORNADO
        );
    }

}
