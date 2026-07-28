package br.com.api.pedidos.payment.webhook.document.entity;

import br.com.api.pedidos.payment.entity.StatusPagamento;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Document(collection = "registro_operacional_webhook_pagamento")
public class RegistroOperacionalWebhookPagamento {

    private static final int TAMANHO_MAXIMO_EVENT_ID = 150;
    private static final int TAMANHO_MAXIMO_CODIGO_TRANSACAO = 150;
    private static final int TAMANHO_MAXIMO_TIPO_EVENTO = 100;
    private static final int TAMANHO_MAXIMO_ORIGEM = 100;
    private static final int TAMANHO_MAXIMO_REQUEST_ID = 200;
    private static final int TAMANHO_MAXIMO_MENSAGEM_ERRO = 2000;

    private static final int TAMANHO_MAXIMO_PAYLOAD = 1_000_000;

    private static final String MENSAGEM_ERRO_NAO_INFORMADA =
            "Erro não informado";

    @Id
    private String id;

    @Indexed(name = "idx_registro_operacional_webhook_event_id")
    private String eventId;

    @Indexed(name = "idx_reistro_operacional_webhook_codigo_transacao")
    private String codigoTransacao;

    private StatusPagamento statusRecebido;

    @Indexed(name = "idx_registro_operacional_webhook_status_processamento")
    private StatusRegistroOperacionalWebhook statusProcessamento;

    private String payloadOriginal;

    @Indexed(name = "idx_registro_operacional_webhook_request_id")
    private String requestId;

    private String tipoEvento;

    private String origem;

    @Indexed(name = "idx_registro_operacional_webhook_data_recebimento")
    private Instant dataRecebimento;

    private Instant dataProcessamento;

    private Long duracaoProcessamentoMs;

    private boolean duplicado;

    private String mensagemErro;

    protected RegistroOperacionalWebhookPagamento() {
    }

    public RegistroOperacionalWebhookPagamento(
            String eventId,
            String codigoTransacao,
            StatusPagamento statusRecebido,
            String payloadOriginal,
            String requestId,
            String tipoEvento,
            String origem
    ) {
        this.eventId = validarENormalizarTextoObrigatorio(
                eventId,
                "EventId do webhook",
                TAMANHO_MAXIMO_EVENT_ID
        );

        this.codigoTransacao = validarENormalizarTextoObrigatorio(
                codigoTransacao,
                "Código da transação",
                TAMANHO_MAXIMO_CODIGO_TRANSACAO
        );

        this.statusRecebido = validarStatusRecebido(
                statusRecebido
        );

        this.payloadOriginal = validarPayload(
                payloadOriginal
        );

        this.requestId = normalizarTextoOpcional(
                requestId,
                "Request ID",
                TAMANHO_MAXIMO_REQUEST_ID
        );

        this.tipoEvento = validarENormalizarTextoObrigatorio(
                tipoEvento,
                "Tipo do evento",
                TAMANHO_MAXIMO_TIPO_EVENTO
        );

        this.origem = validarENormalizarTextoObrigatorio(
                origem,
                "Origem do webhook",
                TAMANHO_MAXIMO_ORIGEM
        );

        this.dataRecebimento = Instant.now();
        this.statusProcessamento =
                StatusRegistroOperacionalWebhook.RECEBIDO;
        this.duplicado = false;
    }

    public void marcarComoProcessado() {
        Instant momentoProcessamento = Instant.now();
        this.statusProcessamento = StatusRegistroOperacionalWebhook.PROCESSADO;
        this.dataProcessamento = momentoProcessamento;
        this.duracaoProcessamentoMs =
                calcularDuracaoEmMilisegundos(momentoProcessamento);
        this.duplicado = false;
        this.mensagemErro = null;
    }

    public void marcarComoDuplicado() {
        Instant momentoProcessamento = Instant.now();
        this.statusProcessamento = StatusRegistroOperacionalWebhook.DUPLICADO;
        this.dataProcessamento = momentoProcessamento;
        this.duracaoProcessamentoMs =
                calcularDuracaoEmMilisegundos(momentoProcessamento);
        this.duplicado = true;
        this.mensagemErro = null;
    }

    public void marcarComoErro(String mensagemErro) {
        Instant momentoProcessamento = Instant.now();
        this.statusProcessamento = StatusRegistroOperacionalWebhook.ERRO;
        this.dataProcessamento = momentoProcessamento;
        this.duracaoProcessamentoMs =
                calcularDuracaoEmMilisegundos(momentoProcessamento);
        this.duplicado = false;
        this.mensagemErro = limitarMensagemErro(mensagemErro);
    }

    public boolean estaRecebido() {
        return statusProcessamento
                == StatusRegistroOperacionalWebhook.RECEBIDO;
    }

    public boolean estaProcessado() {
        return statusProcessamento
                == StatusRegistroOperacionalWebhook.PROCESSADO;
    }

    public boolean estaDuplicado() {
        return statusProcessamento
                == StatusRegistroOperacionalWebhook.DUPLICADO;
    }

    public boolean estaComErro() {
        return statusProcessamento
                == StatusRegistroOperacionalWebhook.ERRO;
    }

    public boolean estaFinalizado() {
        return statusProcessamento != null
                && statusProcessamento.estaFinalizado();
    }

    private Long calcularDuracaoEmMilisegundos(
            Instant momentoProcessamento
    ) {
        if(dataRecebimento == null || momentoProcessamento == null) {
            return null;
        }

        long duracao = Duration.between(
                dataRecebimento,
                momentoProcessamento
        ).toMillis();

        return Math.max(duracao, 0L);
    }

    private String limitarMensagemErro(String mensagemErro) {
        if (mensagemErro == null || mensagemErro.isBlank()) {
            return MENSAGEM_ERRO_NAO_INFORMADA;
        }

        String mensagemNormalizada = mensagemErro.trim();

        if (mensagemNormalizada.length()
                <= TAMANHO_MAXIMO_MENSAGEM_ERRO) {
            return mensagemNormalizada;
        }

        return mensagemNormalizada.substring(
                0,
                TAMANHO_MAXIMO_MENSAGEM_ERRO
        );
    }

    private String validarENormalizarTextoObrigatorio(
            String valor,
            String nomeCampo,
            int tamanhoMaximo
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    nomeCampo + " é obrigatório"
            );
        }

        String valorNormalizado = valor.trim();

        if (valorNormalizado.length() > tamanhoMaximo) {
            throw new IllegalArgumentException(
                    nomeCampo
                            + " deve possuir no máximo "
                            + tamanhoMaximo
                            + " caracteres"
            );
        }

        return valorNormalizado;
    }

    private String normalizarTextoOpcional(
            String valor,
            String nomeCampo,
            int tamanhoMaximo
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String valorNormalizado = valor.trim();

        if (valorNormalizado.length() > tamanhoMaximo) {
            throw new IllegalArgumentException(
                    nomeCampo
                            + " deve possuir no máximo "
                            + tamanhoMaximo
                            + " caracteres"
            );
        }

        return valorNormalizado;
    }

    private StatusPagamento validarStatusRecebido(
            StatusPagamento statusRecebido
    ) {
        if (statusRecebido == null) {
            throw new IllegalArgumentException(
                    "Status recebido é obrigatório"
            );
        }

        return statusRecebido;
    }

    private String validarPayload(String payloadOriginal) {
        if (payloadOriginal == null || payloadOriginal.isBlank()) {
            throw new IllegalArgumentException(
                    "Payload original do webhook é obrigatório"
            );
        }

        if (payloadOriginal.length() > TAMANHO_MAXIMO_PAYLOAD) {
            throw new IllegalArgumentException(
                    "Payload original do webhook excede o tamanho permitido"
            );
        }

        return payloadOriginal;
    }

    public String getId() {
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

    public StatusRegistroOperacionalWebhook getStatusProcessamento() {
        return statusProcessamento;
    }

    public String getPayloadOriginal() {
        return payloadOriginal;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public String getOrigem() {
        return origem;
    }

    public Instant getDataRecebimento() {
        return dataRecebimento;
    }

    public Instant getDataProcessamento() {
        return dataProcessamento;
    }

    public Long getDuracaoProcessamentoMs() {
        return duracaoProcessamentoMs;
    }

    public boolean isDuplicado() {
        return duplicado;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RegistroOperacionalWebhookPagamento that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RegistroOperacionalWebhookPagamento{" +
                "id='" + id + '\'' +
                ", eventId='" + eventId + '\'' +
                ", codigoTransacao='" + codigoTransacao + '\'' +
                ", statusRecebido=" + statusRecebido +
                ", statusProcessamento=" + statusProcessamento +
                ", requestId='" + requestId + '\'' +
                ", tipoEvento='" + tipoEvento + '\'' +
                ", origem='" + origem + '\'' +
                ", dataRecebimento=" + dataRecebimento +
                ", dataProcessamento=" + dataProcessamento +
                ", duracaoProcessamentoMs="
                + duracaoProcessamentoMs +
                ", duplicado=" + duplicado +
                '}';
    }
}
