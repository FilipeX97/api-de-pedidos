package br.com.api.pedidos.payment.strategy;

import br.com.api.pedidos.payment.adapter.GatewayPixAdapter;
import br.com.api.pedidos.payment.adapter.ResultadoPagamento;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import org.springframework.stereotype.Component;

@Component
public class PagamentoPix implements EstrategiaPagamento {

    private final GatewayPixAdapter gatewayPixAdapter;

    public PagamentoPix(GatewayPixAdapter gatewayPixAdapter) {
        this.gatewayPixAdapter = gatewayPixAdapter;
    }

    @Override
    public FormaPagamento getFormaPagamento() {
        return FormaPagamento.PIX;
    }

    @Override
    public ResultadoPagamento processar(Pagamento pagamento) {
        return gatewayPixAdapter.processar(pagamento);
    }
}
