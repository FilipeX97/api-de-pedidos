package br.com.api.pedidos.payment.strategy;

import br.com.api.pedidos.payment.adapter.GatewayCartaoAdapter;
import br.com.api.pedidos.payment.adapter.ResultadoPagamento;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoCartaoCreditoTest {

    @Mock
    private GatewayCartaoAdapter gatewayCartaoAdapter;

    @Mock
    private Pagamento pagamento;

    @InjectMocks
    private PagamentoCartaoCredito estrategia;

    @Test
    void deveRepresentarFormaDePagamentoCartaoCredito() {
        assertEquals(
                FormaPagamento.CARTAO_CREDITO,
                estrategia.getFormaPagamento()
        );
    }

    @Test
    void deveDelegarProcessamentoParaGatewayCartaoAdapter() {
        ResultadoPagamento resultadoEsperado =
                new ResultadoPagamento(
                        StatusPagamento.APROVADO,
                        "CARD-123",
                        "Pagamento autorizado"
                );

        when(gatewayCartaoAdapter.processar(pagamento))
                .thenReturn(resultadoEsperado);

        ResultadoPagamento resultado =
                estrategia.processar(pagamento);

        assertSame(
                resultadoEsperado,
                resultado
        );

        verify(gatewayCartaoAdapter).processar(pagamento);
    }
}