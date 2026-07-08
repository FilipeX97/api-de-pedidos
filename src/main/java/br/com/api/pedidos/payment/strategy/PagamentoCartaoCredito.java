package br.com.api.pedidos.payment.strategy;

import br.com.api.pedidos.payment.adapter.GatewayCartaoAdapter;
import br.com.api.pedidos.payment.adapter.ResultadoPagamento;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import org.springframework.stereotype.Component;

@Component
public class PagamentoCartaoCredito implements EstrategiaPagamento {

    private final GatewayCartaoAdapter gatewayCartaoAdapter;

    public PagamentoCartaoCredito(GatewayCartaoAdapter gatewayCartaoAdapter) {
        this.gatewayCartaoAdapter = gatewayCartaoAdapter;
    }

    @Override
    public FormaPagamento getFormaPagamento() {
        return FormaPagamento.CARTAO_CREDITO;
    }

    @Override
    public ResultadoPagamento processar(Pagamento pagamento) {
        return gatewayCartaoAdapter.processar(pagamento);
    }
}
