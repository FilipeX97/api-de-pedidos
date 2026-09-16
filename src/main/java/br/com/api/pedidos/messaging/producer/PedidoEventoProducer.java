package br.com.api.pedidos.messaging.producer;

import br.com.api.pedidos.messaging.config.RabbitMqNomes;
import br.com.api.pedidos.messaging.dto.PedidoEventoMensagem;
import br.com.api.pedidos.observability.metrics.MetricasRabbitMqService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventoProducer {

    private final RabbitTemplate rabbitTemplate;
    private final MetricasRabbitMqService metricasRabbitMqService;

    public PedidoEventoProducer(
            RabbitTemplate rabbitTemplate,
            MetricasRabbitMqService metricasRabbitMqService
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.metricasRabbitMqService = metricasRabbitMqService;
    }

    public void publicar(
            PedidoEventoMensagem mensagem,
            String routingKey
    ) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqNomes.EXCHANGE_EVENTOS,
                    routingKey,
                    mensagem
            );

            metricasRabbitMqService.registrarMensagemPublicada();

        } catch (RuntimeException e) {
            metricasRabbitMqService.registrarErroProcessamento();
            throw e;
        }
    }
}
