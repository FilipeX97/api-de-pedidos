package br.com.api.pedidos.payment.adapter;

import br.com.api.pedidos.payment.adapter.fake.GatewayCartaoFake;
import br.com.api.pedidos.payment.adapter.fake.RespostaCartaoGateway;
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
class GatewayCartaoAdapterTest {

    @Mock
    private GatewayCartaoFake gatewayCartaoFake;

    @Mock
    private Pagamento pagamento;

    @InjectMocks
    private GatewayCartaoAdapter adapter;

    @Test
    void deveRepresentarFormaDePagamentoCartaoCredito() {
        assertEquals(
                FormaPagamento.CARTAO_CREDITO,
                adapter.getFormaPagamento()
        );
    }

    @Test
    void deveConverterRespostaAutorizadaParaPagamentoAprovado() {
        BigDecimal valor = new BigDecimal("1000.00");

        RespostaCartaoGateway respostaGateway =
                new RespostaCartaoGateway(
                        true,
                        "CARD-APROVADO-123",
                        "Pagamento autorizado pela operadora"
                );

        when(pagamento.getValor()).thenReturn(valor);
        when(gatewayCartaoFake.autorizar(valor)).thenReturn(respostaGateway);

        ResultadoPagamento resultado = adapter.processar(pagamento);

        assertAll(
                () -> assertEquals(
                        StatusPagamento.APROVADO,
                        resultado.statusPagamento()
                ),
                () -> assertEquals(
                        "CARD-APROVADO-123",
                        resultado.codigoTransacao()
                ),
                () -> assertEquals(
                        "Pagamento autorizado pela operadora",
                        resultado.mensagem()
                )
        );

        verify(gatewayCartaoFake).autorizar(valor);
    }

    @Test
    void deveConverterRespostaNaoAutorizadaParaPagamentoRecusado() {
        BigDecimal valor = new BigDecimal("6000.00");

        RespostaCartaoGateway respostaGateway =
                new RespostaCartaoGateway(
                        false,
                        "CARD-RECUSADO-123",
                        "Pagamento recusado pela operadora"
                );

        when(pagamento.getValor()).thenReturn(valor);
        when(gatewayCartaoFake.autorizar(valor)).thenReturn(respostaGateway);

        ResultadoPagamento resultado = adapter.processar(pagamento);

        assertAll(
                () -> assertEquals(
                        StatusPagamento.RECUSADO,
                        resultado.statusPagamento()
                ),
                () -> assertEquals(
                        "CARD-RECUSADO-123",
                        resultado.codigoTransacao()
                ),
                () -> assertEquals(
                        "Pagamento recusado pela operadora",
                        resultado.mensagem()
                )
        );

        verify(gatewayCartaoFake).autorizar(valor);
    }
}