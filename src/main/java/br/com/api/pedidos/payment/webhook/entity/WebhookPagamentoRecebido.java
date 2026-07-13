package br.com.api.pedidos.payment.webhook.entity;

import br.com.api.pedidos.payment.entity.StatusPagamento;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_webhook_pagamento_event_id",
                        columnNames = "event_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_webhook_pagamento_codigo_transacao",
                        columnList = "codigo_transacao"
                ),
                @Index(
                        name = "idx_webhook_pagamento_data_recebimento",
                        columnList = "data_recebimento"
                )
        }
)
public class WebhookPagamentoRecebido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            length = 150,
            updatable = false
    )
    private String eventId;

    @Column(
            name = "codigo_transacao",
            nullable = false,
            length = 150,
            updatable = false
    )
    private String codigoTransacao;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status_recebido",
            nullable = false,
            length = 30,
            updatable = false
    )
    private StatusPagamento statusRecebido;

    @Lob
    @Column(
            name = "payload",
            nullable = false,
            updatable = false
    )
    private String payload;

    @Column(
            name = "data_recebimento",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataRecebimento;

    protected WebhookPagamentoRecebido() {
    }

    public WebhookPagamentoRecebido(
            String eventId,
            String codigoTransacao,
            StatusPagamento statusRecebido,
            String payload) {
        validarEventId(eventId);
        validarCodigoTransacao(codigoTransacao);
        validarStatusRecebido(statusRecebido);
        validarPayload(payload);

        this.eventId = eventId;
        this.codigoTransacao = codigoTransacao;
        this.statusRecebido = statusRecebido;
        this.payload = payload;
        this.dataRecebimento = LocalDateTime.now();
    }

    private void validarEventId(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("EventId do webhook é obrigatório");
        }
    }

    private void validarCodigoTransacao(String codigoTransacao) {
        if (codigoTransacao == null || codigoTransacao.isBlank()) {
            throw new IllegalArgumentException("Código da transação é obrigatório");
        }
    }

    private void validarStatusRecebido(StatusPagamento statusRecebido) {
        if (statusRecebido == null) {
            throw new IllegalArgumentException("Status recebido é obrigatório");
        }
    }

    private void validarPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Payload do webhook é obrigatório");
        }
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getCodigoTransacao() {
        return codigoTransacao;
    }

    public StatusPagamento getStatusRecebido() {
        return statusRecebido;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getDataRecebimento() {
        return dataRecebimento;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WebhookPagamentoRecebido that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
