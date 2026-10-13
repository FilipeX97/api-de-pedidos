package br.com.api.pedidos.messaging.outbox.scheduler;

import br.com.api.pedidos.messaging.outbox.entity.OutboxEvento;
import br.com.api.pedidos.messaging.outbox.service.OutboxEventoService;
import br.com.api.pedidos.messaging.producer.PedidoEventoKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Profile("!test")
public class OutboxPublicador {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublicador.class);

    private final OutboxEventoService outboxEventoService;
    private final PedidoEventoKafkaProducer kafkaProducer;

    public OutboxPublicador(
            OutboxEventoService outboxEventoService,
            PedidoEventoKafkaProducer kafkaProducer
    ) {
        this.outboxEventoService = outboxEventoService;
        this.kafkaProducer = kafkaProducer;
    }

    @Scheduled(
            fixedDelayString = "${app.kafka.outbox.intervalo-ms:1000}"
    )
    public void publicarPendencias() {
        List<OutboxEvento> eventos = outboxEventoService.buscarPendentes();

        for (OutboxEvento evento : eventos) {
            if (!publicar(evento)) {
                break;
            }
        }
    }

    private boolean publicar(OutboxEvento evento) {
        try {
            var mensagem = outboxEventoService.reconstruirMensagem(evento);

            kafkaProducer.publicar(
                    evento.getTopico(),
                    evento.getChaveEvento(),
                    mensagem
            ).get(10, TimeUnit.SECONDS);

            outboxEventoService.marcarComoPublicado(evento.getId());

            return true;
        } catch (InterruptedException excecao) {
            Thread.currentThread().interrupt();
            registrarFalha(evento, excecao);

            return false;
        } catch (ExecutionException | TimeoutException | RuntimeException excecao) {
            registrarFalha(evento, excecao);

            return false;
        }
    }

    private void registrarFalha(OutboxEvento evento, Exception excecao) {
        log.error(
                "Falha ao publicar evento Outbox no Kafka. " +
                        "id={} tipoEvento={} tentativaAnterior={}",
                evento.getId(),
                evento.getTipoEvento(),
                evento.getTentativas(),
                excecao
        );

        outboxEventoService.registrarFalha(evento.getId(), excecao);
    }
}
