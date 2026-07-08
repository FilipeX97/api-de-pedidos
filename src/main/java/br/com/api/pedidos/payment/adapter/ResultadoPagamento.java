package br.com.api.pedidos.payment.adapter;

import br.com.api.pedidos.payment.entity.StatusPagamento;

public record ResultadoPagamento(
        StatusPagamento statusPagamento,
        String codigoTransacao,
        String mensagem) {
}
