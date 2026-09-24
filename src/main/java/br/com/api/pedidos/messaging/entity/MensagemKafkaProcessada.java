package br.com.api.pedidos.messaging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class MensagemKafkaProcessada {

    @EmbeddedId
    private MensagemKafkaProcessadaId id;

    @Column(nullable = false)
    private LocalDateTime dataProcessamento;

    protected MensagemKafkaProcessada() {
    }

    public MensagemKafkaProcessada(
            UUID idEvento,
            String grupoConsumidor,
            LocalDateTime dataProcessamento
    ) {
        this.id = new MensagemKafkaProcessadaId(
                idEvento,
                grupoConsumidor
        );

        this.dataProcessamento = dataProcessamento;
    }

    public MensagemKafkaProcessadaId getId() {
        return id;
    }

    public LocalDateTime getDataProcessamento() {
        return dataProcessamento;
    }
}
