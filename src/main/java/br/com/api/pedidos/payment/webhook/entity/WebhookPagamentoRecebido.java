package br.com.api.pedidos.payment.webhook.entity;

import br.com.api.pedidos.payment.entity.StatusPagamento;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "webhook_pagamento_recebido",
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
                ),
                @Index(
                        name = "idx_webhook_pagamento_status_processamento",
                        columnList = "status_processamento"
                )
        }
)
public class WebhookPagamentoRecebido {

    private static final int TAMANHO_MAXIMO_MENSAGEM_ERRO = 2000;

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

    @Column(
            name = "payload",
            nullable = false,
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String payload;

    @Column(
            name = "data_recebimento",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataRecebimento;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status_processamento",
            nullable = false,
            length = 30
    )
    private StatusProcessamentoWebhook statusProcessamento;

    @Column(
            name = "mensagem_erro",
            length = TAMANHO_MAXIMO_MENSAGEM_ERRO
    )
    private String mensagemErro;

    @Column(name = "data_processamento")
    private LocalDateTime dataProcessamento;

    protected WebhookPagamentoRecebido() {
    }

    public WebhookPagamentoRecebido(
            String eventId,
            String codigoTransacao,
            StatusPagamento statusRecebido,
            String payload
    ) {
        validarEventId(eventId);
        validarCodigoTransacao(codigoTransacao);
        validarStatusRecebido(statusRecebido);
        validarPayload(payload);

        this.eventId = eventId;
        this.codigoTransacao = codigoTransacao;
        this.statusRecebido = statusRecebido;
        this.payload = payload;
        this.dataRecebimento = LocalDateTime.now();
        this.statusProcessamento = StatusProcessamentoWebhook.RECEBIDO;
    }

    public void marcarComoProcessado() {
        this.statusProcessamento = StatusProcessamentoWebhook.PROCESSADO;
        this.mensagemErro = null;
        this.dataProcessamento = LocalDateTime.now();
    }

    public void marcarComoErro(String mensagemErro) {
        this.statusProcessamento = StatusProcessamentoWebhook.ERRO;
        this.mensagemErro = limitarMensagemErro(mensagemErro);
        this.dataProcessamento = LocalDateTime.now();
    }

    public boolean estaRecebido() {
        return statusProcessamento == StatusProcessamentoWebhook.RECEBIDO;
    }

    public boolean estaProcessado() {
        return statusProcessamento == StatusProcessamentoWebhook.PROCESSADO;
    }

    public boolean estaComErro() {
        return statusProcessamento == StatusProcessamentoWebhook.ERRO;
    }

    private String limitarMensagemErro(String mensagemErro) {
        if (mensagemErro == null || mensagemErro.isBlank()) {
            return "Erro não informado";
        }

        if (mensagemErro.length() <= TAMANHO_MAXIMO_MENSAGEM_ERRO) {
            return mensagemErro;
        }

        return mensagemErro.substring(0, TAMANHO_MAXIMO_MENSAGEM_ERRO);
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

    public StatusProcessamentoWebhook getStatusProcessamento() {
        return statusProcessamento;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public LocalDateTime getDataProcessamento() {
        return dataProcessamento;
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
