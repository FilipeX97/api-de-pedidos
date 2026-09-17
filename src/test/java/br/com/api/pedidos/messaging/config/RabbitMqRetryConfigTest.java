package br.com.api.pedidos.messaging.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitMqRetryConfigTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    RabbitAutoConfiguration.class
                            )
                    )
                    .withPropertyValues(
                            "spring.rabbitmq.host=localhost",
                            "spring.rabbitmq.port=5672",
                            "spring.rabbitmq.listener.simple.retry.enabled=true",
                            "spring.rabbitmq.listener.simple.retry.max-attempts=3",
                            "spring.rabbitmq.listener.simple.retry.initial-interval=100ms",
                            "spring.rabbitmq.listener.simple.retry.max-interval=500ms",
                            "spring.rabbitmq.listener.simple.retry.multiplier=2",
                            "spring.rabbitmq.listener.simple.default-requeue-rejected=false"
                    );

    @Test
    void deveCarregarConfiguracaoDeRetryDoListener() {
        contextRunner.run(context -> {

            assertTrue(
                    context.containsBean("rabbitListenerContainerFactory")
            );

            RabbitProperties properties =
                    context.getBean(RabbitProperties.class);

            assertTrue(
                    properties.getListener()
                            .getSimple()
                            .getRetry()
                            .isEnabled()
            );

            assertEquals(
                    3,
                    properties.getListener()
                            .getSimple()
                            .getRetry()
                            .getMaxAttempts()
            );

            assertEquals(
                    100,
                    properties.getListener()
                            .getSimple()
                            .getRetry()
                            .getInitialInterval()
                            .toMillis()
            );

            assertEquals(
                    500,
                    properties.getListener()
                            .getSimple()
                            .getRetry()
                            .getMaxInterval()
                            .toMillis()
            );

            assertEquals(
                    2.0,
                    properties.getListener()
                            .getSimple()
                            .getRetry()
                            .getMultiplier()
            );

            assertEquals(
                    Boolean.FALSE,
                    properties.getListener()
                            .getSimple()
                            .getDefaultRequeueRejected()
            );
        });
    }
}
