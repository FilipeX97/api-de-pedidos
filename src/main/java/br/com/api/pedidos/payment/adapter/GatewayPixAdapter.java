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

        return new ResultadoPagamento(
                StatusPagamento.PENDENTE,
                resposta.txid(),
                "PIX gerado. QR Code: "
                        + resposta.qrCode()
                        + " | Copia e cola: "
                        + resposta.codigoCopiaECola()
        );
    }
}
