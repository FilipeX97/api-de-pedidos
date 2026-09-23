package br.com.api.pedidos.messaging.producer;

import br.com.api.pedidos.messaging.config.KafkaNomes;
import br.com.api.pedidos.messaging.dto.PedidoEventoKafkaMensagem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class PedidoEventoKafkaProducer {

    private final KafkaTemplate<String, PedidoEventoKafkaMensagem> kafkaTemplate;
    private final String topicoPedidos;

    public PedidoEventoKafkaProducer(
            KafkaTemplate<String, PedidoEventoKafkaMensagem> kafkaTemplate,
            @Value("${app.kafka.topico-pedidos:"
                    + KafkaNomes.TOPICO_EVENTOS_PEDIDOS + "}")
            String topicoPedidos
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicoPedidos = topicoPedidos;
    }

    public CompletableFuture<SendResult<String, PedidoEventoKafkaMensagem>> publicar(
            PedidoEventoKafkaMensagem mensagem
    ) {
        validarMensagem(mensagem);

        String chave = mensagem.idPedido().toString();

        return kafkaTemplate.send(
                topicoPedidos,
                chave,
                mensagem
        );
    }

    public CompletableFuture<SendResult<String, PedidoEventoKafkaMensagem>> publicar(
            String topico,
            String chave,
            PedidoEventoKafkaMensagem mensagem
    ) {
        validarMensagem(mensagem);

        if (topico == null || topico.isBlank()) {
            throw new IllegalArgumentException("Topico Kafka e obrigatorio");
        }

        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("Chave Kafka e obrigatoria");
        }

        return kafkaTemplate.send(
                topico,
                chave,
                mensagem
        );
    }

    private void validarMensagem(PedidoEventoKafkaMensagem mensagem) {
        if (mensagem == null) {
            throw new IllegalArgumentException("Mensagem Kafka e obrigatoria");
        }

        if (mensagem.idEvento() == null) {
            throw new IllegalArgumentException("Id do evento e obrigatorio");
        }

        if (mensagem.idPedido() == null) {
            throw new IllegalArgumentException("Id do pedido e obrigatorio");
        }

        if (mensagem.tipoEvento() == null || mensagem.tipoEvento().isBlank()) {
            throw new IllegalArgumentException("Tipo do evento e obrigatorio");
        }
    }
}
