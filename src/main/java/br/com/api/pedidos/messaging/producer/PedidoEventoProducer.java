package br.com.api.pedidos.messaging.producer;

import br.com.api.pedidos.messaging.config.RabbitMqNomes;
import br.com.api.pedidos.messaging.dto.PedidoEventoMensagem;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventoProducer {

    private final RabbitTemplate rabbitTemplate;

    public PedidoEventoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(PedidoEventoMensagem mensagem, String routingKey) {
        rabbitTemplate.convertAndSend(
                RabbitMqNomes.EXCHANGE_EVENTOS,
                routingKey,
                mensagem
        );
    }

}
