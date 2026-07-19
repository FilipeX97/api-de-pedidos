package br.com.api.pedidos.payment.dto;

import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(
        name = "PagamentoResponse",
        description = "Dados de um pagamento processado para um pedido"
)
public record PagamentoResponseDTO(
        @Schema(
                description = "Identificador único do pagamento",
                example = "15"
        )
        Long idPagamento,

        @Schema(
                description = "Identificador do pedido pago",
                example = "10"
        )
        Long idPedido,

        @Schema(
                description = "Valor processado no pagamento",
                example = "300.00"
        )
        BigDecimal valor,

        @Schema(
                description = "Forma utilizada para processar o pagamento",
                example = "PIX",
                implementation = FormaPagamento.class
        )
        FormaPagamento formaPagamento,

        @Schema(
                description = "Estado atual do pagamento",
                example = "PENDENTE",
                implementation = StatusPagamento.class
        )
        StatusPagamento statusPagamento,

        @Schema(
                description = """
                        Código gerado pelo gateway para identificar
                        a transação
                        """,
                example = "PIX-550e8400-e29b-41d4-a716-446655440000"
        )
        String codigoTransacao,

        @Schema(
                description = """
                        Mensagem devolvida pelo gateway de pagamento.

                        Este campo não é a mensagem do envelope RespostaApi.
                        """
        )
        String mensagem,

        @Schema(
                description = "Data e hora da criação do pagamento",
                example = "2026-07-18T14:45:00",
                format = "date-time"
        )
        LocalDateTime dataCriacao
) {
    public static PagamentoResponseDTO from(Pagamento pagamento) {
        return new PagamentoResponseDTO(
                pagamento.getId(),
                pagamento.getIdPedido(),
                pagamento.getValor(),
                pagamento.getFormaPagamento(),
                pagamento.getStatusPagamento(),
                pagamento.getCodigoTransacao(),
                pagamento.getMensagemRetorno(),
                pagamento.getDataCriacao()
        );
    }
}
