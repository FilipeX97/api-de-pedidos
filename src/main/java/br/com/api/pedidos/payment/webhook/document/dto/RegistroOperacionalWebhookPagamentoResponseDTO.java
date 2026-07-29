package br.com.api.pedidos.payment.webhook.document.dto;

import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.webhook.document.entity.RegistroOperacionalWebhookPagamento;
import br.com.api.pedidos.payment.webhook.document.entity.StatusRegistroOperacionalWebhook;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(
        name = "RegistroOperacionalWebhookPagamentoResponse",
        description = """
                Representação administrativa de uma tentativa operacional
                de webhook de pagamento armazenada no MongoDB
                """
)
public record RegistroOperacionalWebhookPagamentoResponseDTO(
        @Schema(
                description = "Identificador do documento no MongoDB",
                example = "6888c15bd39d9a67112f4567"
        )
        String id,

        @Schema(
                description = "Identificador do evento informado pelo gateway",
                example = "evt-123"
        )
        String eventId,

        @Schema(
                description = "Código da transação de pagamento",
                example = "PIX-123"
        )
        String codigoTransacao,

        @Schema(
                description = "Status do pagamento recebido no webhook",
                example = "APROVADO",
                implementation = StatusPagamento.class
        )
        StatusPagamento statusRecebido,

        @Schema(
                description = """
                        Resultado do processamento desta tentativa
                        operacional
                        """,
                example = "PROCESSADO",
                implementation = StatusRegistroOperacionalWebhook.class
        )
        StatusRegistroOperacionalWebhook statusProcessamento,

        @Schema(
                description = """
                        Corpo original recebido no webhook.

                        O acesso é restrito a administradores e o conteúdo
                        não inclui a assinatura HMAC.
                        """,
                example = """
                        {
                          "eventId": "evt-123",
                          "tipo": "PAYMENT_UPDATED",
                          "codigoTransacao": "PIX-123",
                          "statusPagamento": "APROVADO"
                        }
                        """
        )
        String payloadOriginal,

        @Schema(
                description = """
                        Identificador utilizado para correlacionar o documento
                        com os logs da requisição
                        """,
                example = "request-abc-123",
                nullable = true
        )
        String requestId,

        @Schema(
                description = "Tipo do evento recebido",
                example = "PAYMENT_UPDATED"
        )
        String tipoEvento,

        @Schema(
                description = "Sistema que originou o webhook",
                example = "FAKE_GATEWAY"
        )
        String origem,

        @Schema(
                description = "Instante em que a tentativa foi recebida",
                example = "2026-07-28T20:30:00Z",
                type = "string",
                format = "date-time"
        )
        Instant dataRecebimento,

        @Schema(
                description = """
                        Instante em que a tentativa recebeu seu resultado
                        operacional
                        """,
                example = "2026-07-28T20:30:01Z",
                type = "string",
                format = "date-time",
                nullable = true
        )
        Instant dataProcessamento,

        @Schema(
                description = """
                        Tempo decorrido entre o recebimento e o resultado
                        operacional, em milissegundos
                        """,
                example = "180",
                minimum = "0",
                nullable = true
        )
        Long duracaoProcessamentoMs,

        @Schema(
                description = """
                        Indica se o eventId já existia no controle
                        transacional do PostgreSQL
                        """,
                example = "false"
        )
        boolean duplicado,

        @Schema(
                description = """
                        Mensagem técnica resumida quando o processamento
                        termina com erro
                        """,
                example = "Transação do gateway não encontrada",
                nullable = true
        )
        String mensagemErro
) {
    public static RegistroOperacionalWebhookPagamentoResponseDTO from(
            RegistroOperacionalWebhookPagamento registro
    ) {
        if (registro == null) {
            throw new IllegalArgumentException(
                    "Registro operacional é obrigatório"
            );
        }

        return new RegistroOperacionalWebhookPagamentoResponseDTO(
                registro.getId(),
                registro.getEventId(),
                registro.getCodigoTransacao(),
                registro.getStatusRecebido(),
                registro.getStatusProcessamento(),
                registro.getPayloadOriginal(),
                registro.getRequestId(),
                registro.getTipoEvento(),
                registro.getOrigem(),
                registro.getDataRecebimento(),
                registro.getDataProcessamento(),
                registro.getDuracaoProcessamentoMs(),
                registro.isDuplicado(),
                registro.getMensagemErro()
        );
    }
}
