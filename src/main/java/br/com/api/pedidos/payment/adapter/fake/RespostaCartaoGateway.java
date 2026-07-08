package br.com.api.pedidos.payment.adapter.fake;

public record RespostaCartaoGateway(
        boolean autorizado,
        String codigoAutorizacao,
        String mensagem) {
}
