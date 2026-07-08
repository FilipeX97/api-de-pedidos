package br.com.api.pedidos.payment.strategy;

import br.com.api.pedidos.payment.adapter.ResultadoPagamento;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;

public interface EstrategiaPagamento {
    FormaPagamento getFormaPagamento();
    ResultadoPagamento processar(Pagamento pagamento);
}
