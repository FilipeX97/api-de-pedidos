package br.com.api.pedidos.payment.adapter;

import br.com.api.pedidos.payment.adapter.fake.GatewayPixFake;
import br.com.api.pedidos.payment.adapter.fake.RespostaPixGateway;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayPixAdapterTest {

    @Mock
    private GatewayPixFake gatewayPixFake;

    @Mock
    private Pagamento pagamento;

    @InjectMocks
    private GatewayPixAdapter adapter;

    @Test
    void deveRepresentarFormaDePagamentoPix() {
        assertEquals(
                FormaPagamento.PIX,
                adapter.getFormaPagamento()
        );
    }

    @Test
    void deveConverterRespostaDoGatewayPixParaResultadoPendente() {
        BigDecimal valor = new BigDecimal("250.00");

        RespostaPixGateway respostaGateway =
                new RespostaPixGateway(
                        "PIX-123",
                        "000201-COPIA-E-COLA",
                        "QR-CODE-FAKE-PIX-123"
                );

        when(pagamento.getValor()).thenReturn(valor);
        when(gatewayPixFake.cobrar(valor)).thenReturn(respostaGateway);

        ResultadoPagamento resultado = adapter.processar(pagamento);

        assertAll(
                () -> assertEquals(
                        StatusPagamento.PENDENTE,
                        resultado.statusPagamento()
                ),
                () -> assertEquals(
                        "PIX-123",
                        resultado.codigoTransacao()
                ),
                () -> assertEquals(
                        """
                        PIX gerado. QR Code: QR-CODE-FAKE-PIX-123 | Copia e cola: 000201-COPIA-E-COLA\
                        """,
                        resultado.mensagem()
                )
        );

        verify(gatewayPixFake).cobrar(valor);
    }
}