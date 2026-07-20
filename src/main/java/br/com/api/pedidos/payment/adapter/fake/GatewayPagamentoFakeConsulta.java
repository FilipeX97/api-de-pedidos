package br.com.api.pedidos.payment.adapter.fake;

import br.com.api.pedidos.payment.adapter.fake.entity.TransacaoGatewayFake;
import br.com.api.pedidos.payment.adapter.fake.repository.TransacaoGatewayFakeRepository;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GatewayPagamentoFakeConsulta {

    private static final int TAMANHO_MAXIMO_CODIGO_TRANSACAO = 100;

    private final TransacaoGatewayFakeRepository transacaoRepository;

    public GatewayPagamentoFakeConsulta(TransacaoGatewayFakeRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional
    public void registrarPagamentoPendente(String codigoTransacao) {
        validarCodigoTransacao(codigoTransacao);

        if (transacaoRepository.existsByCodigoTransacao(codigoTransacao)) {
            throw new IllegalStateException("Transação já registrada no gateway fake");
        }

        TransacaoGatewayFake transacao = new TransacaoGatewayFake(codigoTransacao);

        transacaoRepository.saveAndFlush(transacao);
    }

    @Transactional
    public void simularAtualizacaoExterna(
            String codigoTransacao,
            StatusPagamento novoStatus
    ) {
        validarCodigoTransacao(codigoTransacao);
        validarStatus(novoStatus);

        TransacaoGatewayFake transacao = buscarTransacao(codigoTransacao);
        transacao.atualizarStatus(novoStatus);
        transacaoRepository.saveAndFlush(transacao);
    }

    @Transactional(readOnly = true)
    public StatusPagamento consultarStatus(String codigoTransacao) {
        validarCodigoTransacao(codigoTransacao);
        return buscarTransacao(codigoTransacao).getStatusPagamento();
    }

    private TransacaoGatewayFake buscarTransacao(String codigoTransacao) {
        return transacaoRepository
                .findByCodigoTransacao(codigoTransacao)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Transação não encontrada no gateway fake"
                        )
                );
    }

    private void validarCodigoTransacao(String codigoTransacao) {
        if (codigoTransacao == null || codigoTransacao.isBlank()) {
            throw new IllegalArgumentException("Código da transação é obrigatório");
        }

        if (codigoTransacao.length() > TAMANHO_MAXIMO_CODIGO_TRANSACAO) {
            throw new IllegalArgumentException(
                    "Código da transação deve ter no máximo "
                            + TAMANHO_MAXIMO_CODIGO_TRANSACAO
                            + " caracteres"
            );
        }
    }

    private void validarStatus(StatusPagamento statusPagamento) {
        if (statusPagamento == null) {
            throw new IllegalArgumentException("Status do pagamento é obrigatório");
        }
    }
}
