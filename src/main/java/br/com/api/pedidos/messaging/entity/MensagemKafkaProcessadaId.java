package br.com.api.pedidos.messaging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class MensagemKafkaProcessadaId implements Serializable {

    @Column(nullable = false)
    private UUID idEvento;

    @Column(nullable = false, length = 100)
    private String grupoConsumidor;

    protected MensagemKafkaProcessadaId() {
    }

    public MensagemKafkaProcessadaId(
            UUID idEvento,
            String grupoConsumidor
    ) {
        this.idEvento = idEvento;
        this.grupoConsumidor = grupoConsumidor;
    }

    public UUID getIdEvento() {
        return idEvento;
    }

    public String getGrupoConsumidor() {
        return grupoConsumidor;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MensagemKafkaProcessadaId that)) return false;
        return Objects.equals(idEvento, that.idEvento) && Objects.equals(grupoConsumidor, that.grupoConsumidor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEvento, grupoConsumidor);
    }
}
