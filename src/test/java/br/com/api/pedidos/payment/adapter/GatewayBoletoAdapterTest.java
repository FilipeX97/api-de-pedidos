package br.com.api.pedidos.payment.adapter;

import br.com.api.pedidos.payment.adapter.fake.GatewayBoletoFake;
import br.com.api.pedidos.payment.adapter.fake.RespostaBoletoGateway;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayBoletoAdapterTest {

    @Mock
    private GatewayBoletoFake gatewayBoletoFake;

    @Mock
    private Pagamento pagamento;

    @InjectMocks
    private GatewayBoletoAdapter adapter;

    @Test
    void deveRepresentarFormaDePagamentoBoleto() {
        assertEquals(
                FormaPagamento.BOLETO,
                adapter.getFormaPagamento()
        );
    }

    @Test
    void deveConverterBoletoGeradoParaResultadoPendente() {
        BigDecimal valor = new BigDecimal("750.00");
        LocalDate vencimento = LocalDate.of(2026, 8, 10);

        RespostaBoletoGateway respostaGateway =
                new RespostaBoletoGateway(
                        "BOL-123",
                        "23790.12345 67890.123456",
                        vencimento,
                        "GERADO"
                );

        when(pagamento.getValor()).thenReturn(valor);
        when(gatewayBoletoFake.gerarBoleto(valor)).thenReturn(respostaGateway);

        ResultadoPagamento resultado = adapter.processar(pagamento);

        assertAll(
                () -> assertEquals(
                        StatusPagamento.PENDENTE,
                        resultado.statusPagamento()
                ),
                () -> assertEquals(
                        "BOL-123",
                        resultado.codigoTransacao()
                ),
                () -> assertEquals(
                        "Boleto gerado. Linha digitável: "
                                + "23790.12345 67890.123456"
                                + " | Vencimento: 2026-08-10",
                        resultado.mensagem()
                )
        );

        verify(gatewayBoletoFake).gerarBoleto(valor);
    }
}