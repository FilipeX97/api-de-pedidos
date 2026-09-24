package br.com.api.pedidos.messaging.consumer;

import br.com.api.pedidos.messaging.config.KafkaNomes;
import br.com.api.pedidos.messaging.dto.PedidoEventoKafkaMensagem;
import br.com.api.pedidos.messaging.service.MensagemKafkaProcessadaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsPedidoKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AnalyticsPedidoKafkaConsumer.class
            );

    private final MensagemKafkaProcessadaService mensagemKafkaProcessadaService;

    public AnalyticsPedidoKafkaConsumer(
            MensagemKafkaProcessadaService mensagemKafkaProcessadaService
    ) {
        this.mensagemKafkaProcessadaService =
                mensagemKafkaProcessadaService;
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(
                    delay = 1000,
                    multiplier = 2,
                    maxDelay = 5000
            ),
            retryTopicSuffix = ".analytics-retry",
            dltTopicSuffix = ".analytics.DLT",
            concurrency = "${app.kafka.retry-concurrency:1}"
    )
    @KafkaListener(
            topics = "${app.kafka.topico-pedidos:"
                    + KafkaNomes.TOPICO_EVENTOS_PEDIDOS + "}",
            groupId = "${app.kafka.grupo-analytics:"
                    + KafkaNomes.GRUPO_ANALYTICS + "}",
            concurrency = "${app.kafka.consumer-concurrency:3}"
    )
    public void receber(
            PedidoEventoKafkaMensagem mensagem,
            ConsumerRecord<String, PedidoEventoKafkaMensagem> registro
    ) {
        validarMensagem(mensagem);

        mensagemKafkaProcessadaService.processar(
                mensagem.idEvento(),
                KafkaNomes.GRUPO_ANALYTICS,
                () -> processarMensagem(mensagem, registro)
        );
    }

    @DltHandler
    public void receberDaDlt(
            PedidoEventoKafkaMensagem mensagem
    ) {
        log.error(
                "Mensagem Kafka de analytics encaminhada para DLT. " +
                        "idEvento={} idPedido={} tipoEvento={}",
                mensagem != null ? mensagem.idEvento() : null,
                mensagem != null ? mensagem.idPedido() : null,
                mensagem != null ? mensagem.tipoEvento() : null
        );
    }

    private void processarMensagem(
            PedidoEventoKafkaMensagem mensagem,
            ConsumerRecord<String, PedidoEventoKafkaMensagem> registro
    ) {
        log.info(
                "Evento Kafka recebido para analytics. " +
                        "topico={} particao={} offset={} idEvento={} " +
                        "idPedido={} tipoEvento={}",
                registro.topic(),
                registro.partition(),
                registro.offset(),
                mensagem.idEvento(),
                mensagem.idPedido(),
                mensagem.tipoEvento()
        );

        switch (mensagem.tipoEvento()) {
            case "PEDIDO_PAGO",
                 "PEDIDO_ENVIADO",
                 "PEDIDO_ENTREGUE",
                 "PEDIDO_CANCELADO",
                 "PEDIDO_ESTORNADO" -> {
                log.info(
                        "Evento de pedido aceito pelo analytics. " +
                                "idPedido={} tipoEvento={} valorFinal={}",
                        mensagem.idPedido(),
                        mensagem.tipoEvento(),
                        mensagem.valorFinal()
                );
            }

            default -> throw new IllegalArgumentException(
                    "Tipo de evento Kafka nao suportado: "
                            + mensagem.tipoEvento()
            );
        }
    }

    private void validarMensagem(
            PedidoEventoKafkaMensagem mensagem
    ) {
        if (mensagem == null) {
            throw new IllegalArgumentException("Mensagem Kafka e obrigatoria");
        }

        if (mensagem.idEvento() == null) {
            throw new IllegalArgumentException("Id do evento e obrigatorio");
        }

        if (mensagem.versao() == null || mensagem.versao() != 1) {
            throw new IllegalArgumentException(
                    "Versao do evento Kafka nao suportada: "
                            + mensagem.versao()
            );
        }

        if (mensagem.tipoEvento() == null || mensagem.tipoEvento().isBlank()) {
            throw new IllegalArgumentException("Tipo do evento e obrigatorio");
        }

        if (mensagem.idPedido() == null) {
            throw new IllegalArgumentException("Id do pedido e obrigatorio");
        }
    }
}
