package br.com.api.pedidos.payment.dto;

import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponseDTO(
        Long idPagamento,
        Long idPedido,
        BigDecimal valor,
        FormaPagamento formaPagamento,
        StatusPagamento statusPagamento,
        String codigoTransacao,
        String mensagem,
        LocalDateTime dataCriacao) {
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
