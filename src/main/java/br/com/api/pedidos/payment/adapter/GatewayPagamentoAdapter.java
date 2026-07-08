package br.com.api.pedidos.payment.adapter;

import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;

public interface GatewayPagamentoAdapter {
    FormaPagamento getFormaPagamento();
    ResultadoPagamento processar(Pagamento pagamento);
}
