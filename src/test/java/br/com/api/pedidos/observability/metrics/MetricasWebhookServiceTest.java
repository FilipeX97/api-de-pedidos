package br.com.api.pedidos.observability.metrics;

import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricasWebhookServiceTest {

    private MockClock clock;
    private SimpleMeterRegistry meterRegistry;

    private MetricasWebhookService
            metricasWebhookService;

    @BeforeEach
    void setUp() {
        clock = new MockClock();

        meterRegistry = new SimpleMeterRegistry(
                SimpleConfig.DEFAULT,
                clock
        );

        metricasWebhookService =
                new MetricasWebhookService(
                        meterRegistry
                );
    }

    @AfterEach
    void tearDown() {
        meterRegistry.close();
    }

    @Test
    void deveIncrementarContadoresDeWebhook() {
        metricasWebhookService
                .registrarWebhookRecebido();

        metricasWebhookService
                .registrarWebhookRecebido();

        metricasWebhookService
                .registrarWebhookProcessado();

        metricasWebhookService
                .registrarWebhookDuplicado();

        metricasWebhookService
                .registrarErroWebhook();

        assertAll(
                () -> assertEquals(
                        2.0,
                        buscarContador(
                                "api.pedidos.webhooks.recebidos"
                        )
                ),
                () -> assertEquals(
                        1.0,
                        buscarContador(
                                "api.pedidos.webhooks.processados"
                        )
                ),
                () -> assertEquals(
                        1.0,
                        buscarContador(
                                "api.pedidos.webhooks.duplicados"
                        )
                ),
                () -> assertEquals(
                        1.0,
                        buscarContador(
                                "api.pedidos.webhooks.erros"
                        )
                )
        );
    }

    @Test
    void deveRegistrarDuracaoDoProcessamento() {
        Timer.Sample amostra =
                metricasWebhookService
                        .iniciarMedicaoProcessamento();

        clock.add(
                Duration.ofMillis(350)
        );

        metricasWebhookService
                .finalizarMedicaoProcessamento(
                        amostra
                );

        Timer timer = meterRegistry
                .get(
                        "api.pedidos.webhook."
                                + "processamento.duracao"
                )
                .timer();

        assertAll(
                () -> assertEquals(
                        1L,
                        timer.count()
                ),
                () -> assertEquals(
                        350.0,
                        timer.totalTime(
                                TimeUnit.MILLISECONDS
                        ),
                        0.001
                ),
                () -> assertEquals(
                        350.0,
                        timer.max(
                                TimeUnit.MILLISECONDS
                        ),
                        0.001
                )
        );
    }

    @Test
    void naoDeveFinalizarMedicaoSemAmostra() {
        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metricasWebhookService
                                .finalizarMedicaoProcessamento(
                                        null
                                )
                );

        assertEquals(
                "Amostra da duração é obrigatória",
                excecao.getMessage()
        );
    }

    private double buscarContador(
            String nomeMetrica
    ) {
        return meterRegistry
                .get(nomeMetrica)
                .counter()
                .count();
    }
}
