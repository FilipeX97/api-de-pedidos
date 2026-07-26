package br.com.api.pedidos.payment.adapter.fake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GatewayCartaoFakeTest {

    private GatewayCartaoFake gatewayCartaoFake;

    @BeforeEach
    void setUp() {
        gatewayCartaoFake = new GatewayCartaoFake();
    }

    @Test
    void deveAprovarPagamentoAbaixoDoLimite() {
        RespostaCartaoGateway resposta =
                gatewayCartaoFake.autorizar(
                        new BigDecimal("4999.99")
                );

        assertAll(
                () -> assertTrue(
                        resposta.autorizado()
                ),
                () -> assertNotNull(
                        resposta.codigoAutorizacao()
                ),
                () -> assertTrue(
                        resposta.codigoAutorizacao()
                                .startsWith("CARD-")
                ),
                () -> assertEquals(
                        "Pagamento autorizado pela operadora",
                        resposta.mensagem()
                )
        );
    }

    @Test
    void deveRecusarPagamentoIgualAoLimite() {
        RespostaCartaoGateway resposta =
                gatewayCartaoFake.autorizar(
                        new BigDecimal("5000.00")
                );

        assertAll(
                () -> assertFalse(
                        resposta.autorizado()
                ),
                () -> assertNotNull(
                        resposta.codigoAutorizacao()
                ),
                () -> assertTrue(
                        resposta.codigoAutorizacao()
                                .startsWith("CARD-")
                ),
                () -> assertEquals(
                        "Pagamento recusado pela operadora",
                        resposta.mensagem()
                )
        );
    }

    @Test
    void deveRecusarPagamentoAcimaDoLimite() {
        RespostaCartaoGateway resposta =
                gatewayCartaoFake.autorizar(
                        new BigDecimal("5000.01")
                );

        assertAll(
                () -> assertFalse(
                        resposta.autorizado()
                ),
                () -> assertNotNull(
                        resposta.codigoAutorizacao()
                ),
                () -> assertTrue(
                        resposta.codigoAutorizacao()
                                .startsWith("CARD-")
                ),
                () -> assertEquals(
                        "Pagamento recusado pela operadora",
                        resposta.mensagem()
                )
        );
    }
}