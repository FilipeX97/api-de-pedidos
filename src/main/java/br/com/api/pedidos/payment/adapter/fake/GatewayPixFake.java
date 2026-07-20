package br.com.api.pedidos.payment.adapter.fake;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class GatewayPixFake {

    public RespostaPixGateway cobrar(BigDecimal valor) {
        String txid = "PIX-" + UUID.randomUUID();

        return new RespostaPixGateway(
                txid,
                gerarCodigoCopiaEColaFake(txid, valor),
                "QR-CODE-FAKE-" + txid
        );
    }

    private String gerarCodigoCopiaEColaFake(
            String txid,
            BigDecimal valor
    ) {
        return "000201"
                + "26360014BR.GOV.BCB.PIX"
                + "52040000"
                + "5303986"
                + "540" + valor
                + "5802BR"
                + "62070503"
                + txid;
    }
}
