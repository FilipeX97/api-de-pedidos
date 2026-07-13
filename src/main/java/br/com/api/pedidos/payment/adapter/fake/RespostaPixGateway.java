package br.com.api.pedidos.payment.adapter.fake;

public record RespostaPixGateway(
        String txid,
        String codigoCopiaECola,
        String qrCode) {
}