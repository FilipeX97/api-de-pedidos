package br.com.api.pedidos.payment.adapter.fake;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

@Component
public class GatewayBoletoFake {

    private final Random random = new Random();

    public RespostaBoletoGateway gerarBoleto(BigDecimal valor) {
        String codigoBoleto = "BOL-" + UUID.randomUUID();

        return new RespostaBoletoGateway(
                codigoBoleto,
                gerarLinhaDigitavel(),
                LocalDate.now().plusDays(3),
                "GERADO"
        );
    }

    private String gerarLinhaDigitavel() {
        return "23790."
                + random.nextInt(99999)
                + " "
                + random.nextInt(99999)
                + "."
                + random.nextInt(999999)
                + " "
                + random.nextInt(99999)
                + "."
                + random.nextInt(999999)
                + " "
                + random.nextInt(9)
                + " "
                + random.nextInt(999999999);
    }
}