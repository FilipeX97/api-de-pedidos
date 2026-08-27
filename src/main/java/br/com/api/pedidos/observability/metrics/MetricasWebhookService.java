package br.com.api.pedidos.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class MetricasWebhookService {

    private static final String METRICA_WEBHOOKS_RECEBIDOS =
            "api.pedidos.webhooks.recebidos";

    private static final String METRICA_WEBHOOKS_PROCESSADOS =
            "api.pedidos.webhooks.processados";

    private static final String METRICA_WEBHOOKS_DUPLICADOS =
            "api.pedidos.webhooks.duplicados";

    private static final String METRICA_WEBHOOKS_ERROS =
            "api.pedidos.webhooks.erros";

    private static final String METRICA_DURACAO_PROCESSAMENTO =
            "api.pedidos.webhook.processamento.duracao";

    private final MeterRegistry meterRegistry;

    private final Counter webhooksRecebidos;
    private final Counter webhooksProcessados;
    private final Counter webhooksDuplicados;
    private final Counter webhooksErros;

    private final Timer duracaoProcessamento;

    public MetricasWebhookService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.webhooksRecebidos = Counter
                .builder(METRICA_WEBHOOKS_RECEBIDOS)
                .description("Quantidade total de webhooks recebidos")
                .register(meterRegistry);

        this.webhooksProcessados = Counter
                .builder(METRICA_WEBHOOKS_PROCESSADOS)
                .description("Quantidade total de webhooks processados")
                .register(meterRegistry);

        this.webhooksDuplicados = Counter
                .builder(METRICA_WEBHOOKS_DUPLICADOS)
                .description("Quantidade total de webhooks duplicados")
                .register(meterRegistry);

        this.webhooksErros = Counter
                .builder(METRICA_WEBHOOKS_ERROS)
                .description("Quantidade total de erros no processamento "
                                + "de webhooks")
                .register(meterRegistry);

        this.duracaoProcessamento = Timer
                .builder(METRICA_DURACAO_PROCESSAMENTO)
                .description("Duração do processamento dos webhooks "
                                + "de pagamento")
                .publishPercentileHistogram()
                .serviceLevelObjectives(
                        Duration.ofMillis(100),
                        Duration.ofMillis(250),
                        Duration.ofMillis(500),
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(2)
                )
                .register(meterRegistry);
    }

    public void registrarWebhookRecebido() {
        webhooksRecebidos.increment();
    }

    public void registrarWebhookProcessado() {
        webhooksProcessados.increment();
    }

    public void registrarWebhookDuplicado() {
        webhooksDuplicados.increment();
    }

    public void registrarErroWebhook() {
        webhooksErros.increment();
    }

    public Timer.Sample iniciarMedicaoProcessamento() {
        return Timer.start(meterRegistry);
    }

    public void finalizarMedicaoProcessamento(Timer.Sample amostra) {
        if (amostra == null) {
            throw new IllegalArgumentException("Amostra da duração é obrigatória");
        }

        amostra.stop(duracaoProcessamento);
    }

}
