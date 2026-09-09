package br.com.api.pedidos.messaging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class MensagemProcessada {

    @Id
    @Column(name = "id_mensagem", nullable = false)
    private UUID idMensagem;

    @Column(name = "tipo_mensagem", nullable = false, length = 100)
    private String tipoMensagem;

    @Column(name = "data_processamento", nullable = false)
    private LocalDateTime dataProcessamento;

    protected MensagemProcessada() {
    }

    public MensagemProcessada(
            UUID idMensagem,
            String tipoMensagem,
            LocalDateTime dataProcessamento
    ) {
        this.idMensagem = idMensagem;
        this.tipoMensagem = tipoMensagem;
        this.dataProcessamento = dataProcessamento;
    }

    public UUID getIdMensagem() {
        return idMensagem;
    }

    public String getTipoMensagem() {
        return tipoMensagem;
    }

    public LocalDateTime getDataProcessamento() {
        return dataProcessamento;
    }
}