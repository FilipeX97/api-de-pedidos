package br.com.api.pedidos.messaging.outbox.service;

import br.com.api.pedidos.messaging.dto.PedidoEventoKafkaMensagem;
import br.com.api.pedidos.messaging.outbox.entity.OutboxEvento;
import br.com.api.pedidos.messaging.outbox.entity.StatusOutboxEvento;
import br.com.api.pedidos.messaging.outbox.repository.OutboxEventoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OutboxEventoService {

    private final OutboxEventoRepository repository;
    private final ObjectMapper objectMapper;
    private final String topicoPedidos;

    public OutboxEventoService(
            OutboxEventoRepository repository,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topico-pedidos}") String topicoPedidos
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.topicoPedidos = topicoPedidos;
    }

    @Transactional
    public void registrar(
            PedidoEventoKafkaMensagem mensagem
    ) {
        if (mensagem == null) {
            throw new IllegalArgumentException(
                    "Mensagem Kafka e obrigatoria"
            );
        }

        try {
            String payload =
                    objectMapper.writeValueAsString(mensagem);

            OutboxEvento evento = new OutboxEvento(
                    mensagem.idEvento(),
                    mensagem.tipoEvento(),
                    mensagem.idPedido().toString(),
                    topicoPedidos,
                    payload
            );

            repository.save(evento);

        } catch (JsonProcessingException excecao) {
            throw new IllegalStateException(
                    "Nao foi possivel serializar evento Kafka para Outbox",
                    excecao
            );
        }
    }

    @Transactional(readOnly = true)
    public List<OutboxEvento> buscarPendentes() {
        return repository.findTop50ByStatusOrderByDataCriacaoAsc(
                StatusOutboxEvento.PENDENTE
        );
    }

    public PedidoEventoKafkaMensagem reconstruirMensagem(
            OutboxEvento evento
    ) {
        if (evento == null) {
            throw new IllegalArgumentException(
                    "Evento Outbox e obrigatorio"
            );
        }

        try {
            return objectMapper.readValue(
                    evento.getPayload(),
                    PedidoEventoKafkaMensagem.class
            );
        } catch (JsonProcessingException excecao) {
            throw new IllegalStateException(
                    "Nao foi possivel reconstruir mensagem Kafka da Outbox",
                    excecao
            );
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarComoPublicado(UUID id) {
        OutboxEvento evento = buscar(id);

        evento.marcarComoPublicado(
                LocalDateTime.now()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarFalha(
            UUID id,
            Exception excecao
    ) {
        OutboxEvento evento = buscar(id);

        evento.registrarFalha(
                LocalDateTime.now(),
                excecao.getMessage()
        );
    }

    private OutboxEvento buscar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Evento Outbox nao encontrado: " + id
                        )
                );
    }
}
