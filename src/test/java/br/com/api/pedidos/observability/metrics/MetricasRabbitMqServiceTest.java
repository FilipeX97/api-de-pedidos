package br.com.api.pedidos.observability.metrics;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class MetricasRabbitMqServiceTest {

    @Test
    void deveRegistrarMensagemPublicada() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);

        MetricasRabbitMqService service =
                new MetricasRabbitMqService(
                        registry,
                        rabbitAdmin
                );

        service.registrarMensagemPublicada();

        assertEquals(
                1.0,
                registry
                        .get(
                                "api.pedidos.rabbitmq.mensagens.publicadas"
                        )
                        .counter()
                        .count()
        );
    }

    @Test
    void deveRegistrarMensagemProcessada() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);

        MetricasRabbitMqService service =
                new MetricasRabbitMqService(
                        registry,
                        rabbitAdmin
                );

        service.registrarMensagemProcessada();

        assertEquals(
                1.0,
                registry
                        .get(
                                "api.pedidos.rabbitmq.mensagens.processadas"
                        )
                        .counter()
                        .count()
        );
    }

    @Test
    void deveRegistrarMensagemDuplicada() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);

        MetricasRabbitMqService service =
                new MetricasRabbitMqService(
                        registry,
                        rabbitAdmin
                );

        service.registrarMensagemDuplicada();

        assertEquals(
                1.0,
                registry
                        .get(
                                "api.pedidos.rabbitmq.mensagens.duplicadas"
                        )
                        .counter()
                        .count()
        );
    }

    @Test
    void deveRegistrarErroDeProcessamento() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);

        MetricasRabbitMqService service =
                new MetricasRabbitMqService(
                        registry,
                        rabbitAdmin
                );

        service.registrarErroProcessamento();

        assertEquals(
                1.0,
                registry
                        .get(
                                "api.pedidos.rabbitmq.mensagens.erros"
                        )
                        .counter()
                        .count()
        );
    }

    @Test
    void deveRegistrarTempoDeProcessamento() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);

        MetricasRabbitMqService service =
                new MetricasRabbitMqService(
                        registry,
                        rabbitAdmin
                );

        Timer.Sample amostra = service.iniciarProcessamento(registry);
        service.finalizarProcessamento(amostra);

        assertEquals(
                1,
                registry
                        .get(
                                "api.pedidos.rabbitmq.processamento"
                        )
                        .timer()
                        .count()
        );
    }
}
