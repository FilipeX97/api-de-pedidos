package br.com.api.pedidos.payment.adapter;

import br.com.api.pedidos.payment.adapter.fake.GatewayPixFake;
import br.com.api.pedidos.payment.adapter.fake.RespostaPixGateway;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import org.springframework.stereotype.Component;

@Component
public class GatewayPixAdapter implements GatewayPagamentoAdapter {

    private final GatewayPixFake gatewayPixFake;

    public GatewayPixAdapter(GatewayPixFake gatewayPixFake) {
        this.gatewayPixFake = gatewayPixFake;
    }

    @Override
    public FormaPagamento getFormaPagamento() {
        return FormaPagamento.PIX;
    }

    @Override
    public ResultadoPagamento processar(Pagamento pagamento) {
        RespostaPixGateway resposta =
                gatewayPixFake.cobrar(pagamento.getValor());

        if (resposta.aprovado()) {
            return new ResultadoPagamento(
                    StatusPagamento.APROVADO,
                    resposta.codigoPix(),
                    "PIX aprovado com sucesso"
            );
        }

        return new ResultadoPagamento(
                StatusPagamento.RECUSADO,
                resposta.codigoPix(),
                "PIX recusado"
        );
    }
}
