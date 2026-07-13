package br.com.api.pedidos.payment.adapter.fake;

import java.time.LocalDate;

public record RespostaBoletoGateway(
        String codigoBoleto,
        String linhaDigitavel,
        LocalDate dataVencimento,
        String status) {
}
