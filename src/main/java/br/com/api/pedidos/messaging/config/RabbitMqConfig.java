package br.com.api.pedidos.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import static br.com.api.pedidos.messaging.config.RabbitMqNomes.EXCHANGE_DEAD_LETTER;
import static br.com.api.pedidos.messaging.config.RabbitMqNomes.EXCHANGE_EVENTOS;
import static br.com.api.pedidos.messaging.config.RabbitMqNomes.FILA_NOTIFICACOES_PEDIDO;
import static br.com.api.pedidos.messaging.config.RabbitMqNomes.FILA_NOTIFICACOES_PEDIDO_DLQ;
import static br.com.api.pedidos.messaging.config.RabbitMqNomes.ROUTING_KEY_PEDIDO_CANCELADO;
import static br.com.api.pedidos.messaging.config.RabbitMqNomes.ROUTING_KEY_PEDIDO_ENTREGUE;
import static br.com.api.pedidos.messaging.config.RabbitMqNomes.ROUTING_KEY_PEDIDO_ESTORNADO;
import static br.com.api.pedidos.messaging.config.RabbitMqNomes.ROUTING_KEY_PEDIDO_ENVIADO;
import static br.com.api.pedidos.messaging.config.RabbitMqNomes.ROUTING_KEY_PEDIDO_PAGO;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange exchangeEventos() {
        return new TopicExchange(
                EXCHANGE_EVENTOS,
                true,
                false
        );
    }

    @Bean
    public TopicExchange exchangeDeadLetter() {
        return new TopicExchange(
                EXCHANGE_DEAD_LETTER,
                true,
                false
        );
    }

    @Bean
    public Queue filaNotificacoesPedido() {
        return QueueBuilder
                .durable(FILA_NOTIFICACOES_PEDIDO)
                .deadLetterExchange(EXCHANGE_DEAD_LETTER)
                .deadLetterRoutingKey(FILA_NOTIFICACOES_PEDIDO_DLQ)
                .build();
    }

    @Bean
    public Queue filaNotificacoesPedidoDlq() {
        return QueueBuilder
                .durable(FILA_NOTIFICACOES_PEDIDO_DLQ)
                .build();
    }

    @Bean
    public Binding bindingPedidoPago(
            Queue filaNotificacoesPedido,
            TopicExchange exchangeEventos
    ) {
        return BindingBuilder
                .bind(filaNotificacoesPedido)
                .to(exchangeEventos)
                .with(ROUTING_KEY_PEDIDO_PAGO);
    }

    @Bean
    public Binding bindingPedidoEnviado(
            Queue filaNotificacoesPedido,
            TopicExchange exchangeEventos
    ) {
        return BindingBuilder
                .bind(filaNotificacoesPedido)
                .to(exchangeEventos)
                .with(ROUTING_KEY_PEDIDO_ENVIADO);
    }

    @Bean
    public Binding bindingPedidoEntregue(
            Queue filaNotificacoesPedido,
            TopicExchange exchangeEventos
    ) {
        return BindingBuilder
                .bind(filaNotificacoesPedido)
                .to(exchangeEventos)
                .with(ROUTING_KEY_PEDIDO_ENTREGUE);
    }

    @Bean
    public Binding bindingPedidoCancelado(
            Queue filaNotificacoesPedido,
            TopicExchange exchangeEventos
    ) {
        return BindingBuilder
                .bind(filaNotificacoesPedido)
                .to(exchangeEventos)
                .with(ROUTING_KEY_PEDIDO_CANCELADO);
    }

    @Bean
    public Binding bindingPedidoEstornado(
            Queue filaNotificacoesPedido,
            TopicExchange exchangeEventos
    ) {
        return BindingBuilder
                .bind(filaNotificacoesPedido)
                .to(exchangeEventos)
                .with(ROUTING_KEY_PEDIDO_ESTORNADO);
    }

    @Bean
    public Binding bindingFilaNotificacoesDlq(
            Queue filaNotificacoesPedidoDlq,
            TopicExchange exchangeDeadLetter
    ) {
        return BindingBuilder
                .bind(filaNotificacoesPedidoDlq)
                .to(exchangeDeadLetter)
                .with(FILA_NOTIFICACOES_PEDIDO_DLQ);
    }

    @Bean
    public MessageConverter conversaoDeMensagemDoRabbitMq() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(
            ConnectionFactory connectionFactory
    ) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter conversorDeMensagemDoRabbitMq
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(conversorDeMensagemDoRabbitMq);

        rabbitTemplate.setConfirmCallback(
                (correlationData, ack, cause) -> {
                    if(ack)
                        return;

                    throw new IllegalArgumentException(
                            "RabbitMQ não confirmou a publicação"
                                    + (cause != null
                                    ? ": " + cause
                                    : "")
                    );
                }
        );

        rabbitTemplate.setReturnsCallback(
                returned -> {
                    throw new IllegalStateException(
                            "Mensagem RabbitMQ não foi roteada: "
                                    + returned.getMessage()
                    );
                }
        );

        return rabbitTemplate;
    }

}
