package br.com.api.pedidos.observability.metrics;

import br.com.api.pedidos.messaging.config.RabbitMqNomes;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricasRabbitMqService {

    private static final String METRICA_MENSAGENS_PUBLICADAS =
            "api.pedidos.rabbitmq.mensagens.publicadas";

    private static final String METRICA_MENSAGENS_PROCESSADAS =
            "api.pedidos.rabbitmq.mensagens.processadas";

    private static final String METRICA_MENSAGENS_DUPLICADAS =
            "api.pedidos.rabbitmq.mensagens.duplicadas";

    private static final String METRICA_MENSAGENS_ERRO =
            "api.pedidos.rabbitmq.mensagens.erros";

    private static final String METRICA_PROCESSAMENTO =
            "api.pedidos.rabbitmq.processamento";

    private static final String METRICA_FILA_MENSAGENS =
            "api.pedidos.rabbitmq.fila.mensagens";

    private static final String METRICA_DLQ_MENSAGENS =
            "api.pedidos.rabbitmq.dlq.mensagens";

    private final Counter mensagensPublicadas;
    private final Counter mensagensProcessadas;
    private final Counter mensagensDuplicadas;
    private final Counter mensagensErros;
    private final Timer processamento;
    private final RabbitAdmin rabbitAdmin;
    private final AtomicInteger mensagensNaFila = new AtomicInteger();
    private final AtomicInteger mensagensNaDlq = new AtomicInteger();

    public MetricasRabbitMqService(
            MeterRegistry meterRegistry,
            RabbitAdmin rabbitAdmin
    ) {
        this.rabbitAdmin = rabbitAdmin;

        this.mensagensPublicadas = Counter
                .builder(METRICA_MENSAGENS_PUBLICADAS)
                .description(
                        "Quantidade total de mensagens publicadas"
                )
                .register(meterRegistry);

        this.mensagensProcessadas = Counter
                .builder(METRICA_MENSAGENS_PROCESSADAS)
                .description(
                        "Quantidade total de mensagens processadas"
                )
                .register(meterRegistry);

        this.mensagensDuplicadas = Counter
                .builder(METRICA_MENSAGENS_DUPLICADAS)
                .description(
                        "Quantidade total de mensagens duplicadas"
                )
                .register(meterRegistry);

        this.mensagensErros = Counter
                .builder(METRICA_MENSAGENS_ERRO)
                .description(
                        "Quantidade total de falhas no processamento "
                                + "de mensagens RabbitMQ"
                )
                .register(meterRegistry);

        this.processamento = Timer
                .builder(METRICA_PROCESSAMENTO)
                .description(
                        "Tempo de processamento das mensagens RabbitMQ"
                )
                .publishPercentileHistogram()
                .serviceLevelObjectives(
                        Duration.ofMillis(50),
                        Duration.ofMillis(100),
                        Duration.ofMillis(250),
                        Duration.ofMillis(500),
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(2)
                )
                .register(meterRegistry);

        Gauge.builder(
                        METRICA_FILA_MENSAGENS,
                        mensagensNaFila,
                        AtomicInteger::get
                )
                .description(
                        "Quantidade atual de mensagens na fila "
                                + "de notificacoes"
                )
                .register(meterRegistry);

        Gauge.builder(
                        METRICA_DLQ_MENSAGENS,
                        mensagensNaDlq,
                        AtomicInteger::get
                )
                .description(
                        "Quantidade atual de mensagens na DLQ"
                )
                .register(meterRegistry);
    }

    public void registrarMensagemPublicada() {
        mensagensPublicadas.increment();
    }

    public void registrarMensagemProcessada() {
        mensagensProcessadas.increment();
    }

    public void registrarMensagemDuplicada() {
        mensagensDuplicadas.increment();
    }

    public void registrarErroProcessamento() {
        mensagensErros.increment();
    }

    public Timer.Sample iniciarProcessamento(
            MeterRegistry meterRegistry
    ) {
        return Timer.start(meterRegistry);
    }

    public void finalizarProcessamento(
            Timer.Sample amostra
    ) {
        if(amostra == null) {
            throw new IllegalArgumentException(
                    "Amostra de processamento é obrigatória"
            );
        }

        amostra.stop(processamento);
    }

    @Scheduled(fixedDelay = 15000)
    public void atualizarTamanhoDasFilas() {
        try {
            atualizarFila(
                    RabbitMqNomes.FILA_NOTIFICACOES_PEDIDO,
                    mensagensNaFila
            );

            atualizarFila(
                    RabbitMqNomes.FILA_NOTIFICACOES_PEDIDO_DLQ,
                    mensagensNaDlq
            );
        } catch (Exception e) {
            mensagensNaFila.set(0);
            mensagensNaDlq.set(0);
        }
    }

    private void atualizarFila(
            String nomeFila,
            AtomicInteger destino
    ) {
        var propriedades = rabbitAdmin.getQueueProperties(nomeFila);

        if(propriedades == null) {
            destino.set(0);
            return;
        }

        Object quantidade = propriedades.get(
                RabbitAdmin.QUEUE_MESSAGE_COUNT
        );

        if(quantidade instanceof Number numero) {
            destino.set(numero.intValue());
        } else {
            destino.set(0);
        }
    }
}
