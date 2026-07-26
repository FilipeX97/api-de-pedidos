package br.com.api.pedidos.payment.strategy;

import br.com.api.pedidos.payment.adapter.GatewayPixAdapter;
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
class PagamentoPixTest {

    @Mock
    private GatewayPixAdapter gatewayPixAdapter;

    @Mock
    private Pagamento pagamento;

    @InjectMocks
    private PagamentoPix estrategia;

    @Test
    void deveRepresentarFormaDePagamentoPix() {
        assertEquals(
                FormaPagamento.PIX,
                estrategia.getFormaPagamento()
        );
    }

    @Test
    void deveDelegarProcessamentoParaGatewayPixAdapter() {
        ResultadoPagamento resultadoEsperado =
                new ResultadoPagamento(
                        StatusPagamento.PENDENTE,
                        "PIX-123",
                        "PIX gerado"
                );

        when(gatewayPixAdapter.processar(pagamento))
                .thenReturn(resultadoEsperado);

        ResultadoPagamento resultado =
                estrategia.processar(pagamento);

        assertSame(
                resultadoEsperado,
                resultado
        );

        verify(gatewayPixAdapter).processar(pagamento);
    }
}