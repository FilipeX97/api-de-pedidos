package br.com.api.pedidos.messaging.outbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class OutboxEvento {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String tipoEvento;

    @Column(nullable = false, length = 100)
    private String chaveEvento;

    @Column(nullable = false)
    private String topico;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusOutboxEvento status;

    @Column(nullable = false)
    private Integer tentativas;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataPublicacao;

    private LocalDateTime ultimaTentativa;

    @Column(length = 2000)
    private String ultimaExcecao;

    protected OutboxEvento() {
    }

    public OutboxEvento(
            UUID id,
            String tipoEvento,
            String chaveEvento,
            String topico,
            String payload
    ) {
        this.id = id;
        this.tipoEvento = tipoEvento;
        this.chaveEvento = chaveEvento;
        this.topico = topico;
        this.payload = payload;
        this.status = StatusOutboxEvento.PENDENTE;
        this.tentativas = 0;
        this.dataCriacao = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public String getChaveEvento() {
        return chaveEvento;
    }

    public String getTopico() {
        return topico;
    }

    public String getPayload() {
        return payload;
    }

    public StatusOutboxEvento getStatus() {
        return status;
    }

    public Integer getTentativas() {
        return tentativas;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataPublicacao() {
        return dataPublicacao;
    }

    public LocalDateTime getUltimaTentativa() {
        return ultimaTentativa;
    }

    public String getUltimaExcecao() {
        return ultimaExcecao;
    }

    public void marcarComoPublicado(LocalDateTime dataHora) {
        this.status = StatusOutboxEvento.PUBLICADO;
        this.dataPublicacao = dataHora;
        this.ultimaTentativa = dataHora;
        this.ultimaExcecao = null;
    }

    public void registrarFalha(
            LocalDateTime dataHora,
            String excecao
    ) {
        this.tentativas++;
        this.ultimaTentativa = dataHora;
        this.ultimaExcecao = limitarExcecao(excecao);
    }

    private String limitarExcecao(String excecao) {
        if (excecao == null) {
            return null;
        }

        if (excecao.length() <= 2000) {
            return excecao;
        }

        return excecao.substring(0, 2000);
    }
}
