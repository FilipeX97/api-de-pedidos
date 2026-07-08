package br.com.api.pedidos.payment.adapter;

import br.com.api.pedidos.payment.adapter.fake.GatewayCartaoFake;
import br.com.api.pedidos.payment.adapter.fake.RespostaCartaoGateway;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import org.springframework.stereotype.Component;

@Component
public class GatewayCartaoAdapter implements GatewayPagamentoAdapter {

    private final GatewayCartaoFake gatewayCartaoFake;

    public GatewayCartaoAdapter(GatewayCartaoFake gatewayCartaoFake) {
        this.gatewayCartaoFake = gatewayCartaoFake;
    }

    @Override
    public FormaPagamento getFormaPagamento() {
        return FormaPagamento.CARTAO_CREDITO;
    }

    @Override
    public ResultadoPagamento processar(Pagamento pagamento) {
        RespostaCartaoGateway resposta =
                gatewayCartaoFake.autorizar(pagamento.getValor());

        if (resposta.autorizado()) {
            return new ResultadoPagamento(
                    StatusPagamento.APROVADO,
                    resposta.codigoAutorizacao(),
                    resposta.mensagem()
            );
        }

        return new ResultadoPagamento(
                StatusPagamento.RECUSADO,
                resposta.codigoAutorizacao(),
                resposta.mensagem()
        );
    }
}
