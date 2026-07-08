package br.com.api.pedidos.payment.dto;

import br.com.api.pedidos.payment.entity.FormaPagamento;

public record PagamentoRequestDTO(
        FormaPagamento formaPagamento) {
}
