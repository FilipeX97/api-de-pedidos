package br.com.api.pedidos.payment.strategy;

import br.com.api.pedidos.payment.adapter.GatewayBoletoAdapter;
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
class PagamentoBoletoTest {

    @Mock
    private GatewayBoletoAdapter gatewayBoletoAdapter;

    @Mock
    private Pagamento pagamento;

    @InjectMocks
    private PagamentoBoleto estrategia;

    @Test
    void deveRepresentarFormaDePagamentoBoleto() {
        assertEquals(
                FormaPagamento.BOLETO,
                estrategia.getFormaPagamento()
        );
    }

    @Test
    void deveDelegarProcessamentoParaGatewayBoletoAdapter() {
        ResultadoPagamento resultadoEsperado =
                new ResultadoPagamento(
                        StatusPagamento.PENDENTE,
                        "BOL-123",
                        "Boleto gerado"
                );

        when(gatewayBoletoAdapter.processar(pagamento))
                .thenReturn(resultadoEsperado);

        ResultadoPagamento resultado =
                estrategia.processar(pagamento);

        assertSame(
                resultadoEsperado,
                resultado
        );

        verify(gatewayBoletoAdapter).processar(pagamento);
    }
}