package br.com.api.pedidos.payment.webhook.document.dto;

import br.com.api.pedidos.payment.webhook.document.entity.StatusRegistroOperacionalWebhook;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Schema(
        name = "RegistroOperacionalWebhookPagamentoFiltro",
        description = """
                Filtros opcionais para a consulta administrativa dos
                registros operacionais de webhooks de pagamento.

                Quando nenhum filtro é informado, todos os registros são
                considerados.
                """
)
public record RegistroOperacionalWebhookPagamentoFiltroDTO(
        @Schema(
                description = """
                        Identificador do evento informado pelo gateway.

                        A consulta utiliza correspondência exata.
                        """,
                example = "evt-123",
                maxLength = 150
        )
        @Size(
                max = 150,
                message = "EventId deve possuir no máximo 150 caracteres"
        )
        String eventId,

        @Schema(
                description = """
                        Código da transação de pagamento.

                        A consulta utiliza correspondência exata.
                        """,
                example = "PIX-123",
                maxLength = 150
        )
        @Size(
                max = 150,
                message = """
                        Código da transação deve possuir no máximo
                        150 caracteres
                        """
        )
        String codigoTransacao,

        @Schema(
                description = """
                        Status final ou atual do processamento operacional
                        da tentativa de webhook
                        """,
                example = "PROCESSADO",
                allowableValues = {
                        "RECEBIDO",
                        "PROCESSADO",
                        "DUPLICADO",
                        "ERRO"
                },
                implementation = StatusRegistroOperacionalWebhook.class
        )
        StatusRegistroOperacionalWebhook statusProcessamento,

        @Schema(
                description = """
                        Instante inicial inclusivo do período de recebimento.

                        O valor deve possuir fuso ou offset, normalmente UTC.
                        """,
                example = "2026-07-28T00:00:00Z",
                type = "string",
                format = "date-time"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant dataInicio,

        @Schema(
                description = """
                        Instante final inclusivo do período de recebimento.

                        O valor deve possuir fuso ou offset, normalmente UTC.
                        """,
                example = "2026-07-28T23:59:59Z",
                type = "string",
                format = "date-time"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant dataFim,

        @Schema(
                description = """
                        Filtra tentativas cujo eventId já existia no controle
                        transacional do PostgreSQL
                        """,
                example = "true"
        )
        Boolean duplicado
) {
}
