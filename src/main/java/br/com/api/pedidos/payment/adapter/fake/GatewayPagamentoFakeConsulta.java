package br.com.api.pedidos.payment.adapter.fake;

import br.com.api.pedidos.payment.entity.StatusPagamento;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GatewayPagamentoFakeConsulta {

    private final Map<String, StatusPagamento> statusPorTransacao =
            new ConcurrentHashMap<>();

    public void registrarPagamentoPendente(String codigoTransacao) {
        statusPorTransacao.put(codigoTransacao, StatusPagamento.PENDENTE);
    }

    public void simularAtualizacaoExterna(
            String codigoTransacao,
            StatusPagamento novoStatus) {
        if (codigoTransacao == null || codigoTransacao.isBlank()) {
            throw new IllegalArgumentException("Código da transação é obrigatório");
        }

        if (novoStatus == null) {
            throw new IllegalArgumentException("Status do pagamento é obrigatório");
        }

        if (!statusPorTransacao.containsKey(codigoTransacao)) {
            throw new IllegalArgumentException(
                    "Transação não encontrada no gateway fake"
            );
        }

        statusPorTransacao.put(codigoTransacao, novoStatus);
    }

    public StatusPagamento consultarStatus(String codigoTransacao) {
        StatusPagamento status = statusPorTransacao.get(codigoTransacao);

        if(status == null) {
            throw new IllegalArgumentException(
                    "Transação não encontrada no gateway fake"
            );
        }

        return status;
    }

}
