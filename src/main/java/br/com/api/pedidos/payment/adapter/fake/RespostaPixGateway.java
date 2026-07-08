package br.com.api.pedidos.payment.adapter.fake;

public record RespostaPixGateway(
        String codigoPix,
        boolean aprovado) {
}
