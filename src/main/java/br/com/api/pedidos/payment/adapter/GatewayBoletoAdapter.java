package br.com.api.pedidos.payment.adapter;

import br.com.api.pedidos.payment.adapter.fake.GatewayBoletoFake;
import br.com.api.pedidos.payment.adapter.fake.RespostaBoletoGateway;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import org.springframework.stereotype.Component;

@Component
public class GatewayBoletoAdapter implements GatewayPagamentoAdapter {

    private final GatewayBoletoFake gatewayBoletoFake;

    public GatewayBoletoAdapter(GatewayBoletoFake gatewayBoletoFake) {
        this.gatewayBoletoFake = gatewayBoletoFake;
    }

    @Override
    public FormaPagamento getFormaPagamento() {
        return FormaPagamento.BOLETO;
    }

    @Override
    public ResultadoPagamento processar(Pagamento pagamento) {
        RespostaBoletoGateway resposta =
                gatewayBoletoFake.gerarBoleto(pagamento.getValor());

        return new ResultadoPagamento(
                StatusPagamento.PENDENTE,
                resposta.linhaDigitavel(),
                "Boleto gerado com vencimento em " + resposta.dataVencimento()
        );
    }
}
