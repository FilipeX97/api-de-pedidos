package br.com.api.pedidos.payment.strategy;

import br.com.api.pedidos.payment.adapter.GatewayBoletoAdapter;
import br.com.api.pedidos.payment.adapter.ResultadoPagamento;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import org.springframework.stereotype.Component;

@Component
public class PagamentoBoleto implements EstrategiaPagamento {

    private final GatewayBoletoAdapter gatewayBoletoAdapter;

    public PagamentoBoleto(GatewayBoletoAdapter gatewayBoletoAdapter) {
        this.gatewayBoletoAdapter = gatewayBoletoAdapter;
    }

    @Override
    public FormaPagamento getFormaPagamento() {
        return FormaPagamento.BOLETO;
    }

    @Override
    public ResultadoPagamento processar(Pagamento pagamento) {
        return gatewayBoletoAdapter.processar(pagamento);
    }
}
