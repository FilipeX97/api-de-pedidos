package br.com.api.pedidos.payment.strategy;

import br.com.api.pedidos.payment.entity.FormaPagamento;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EstrategiaPagamentoFactoryTest {

    @Test
    void deveObterEstrategiaCorrespondenteParaCadaFormaPagamento() {
        EstrategiaPagamento estrategiaPix = mock(EstrategiaPagamento.class);
        EstrategiaPagamento estrategiaCartao = mock(EstrategiaPagamento.class);
        EstrategiaPagamento estrategiaBoleto = mock(EstrategiaPagamento.class);

        when(estrategiaPix.getFormaPagamento()).thenReturn(FormaPagamento.PIX);
        when(estrategiaCartao.getFormaPagamento()).thenReturn(FormaPagamento.CARTAO_CREDITO);
        when(estrategiaBoleto.getFormaPagamento()).thenReturn(FormaPagamento.BOLETO);

        EstrategiaPagamentoFactory factory =
                new EstrategiaPagamentoFactory(
                        List.of(
                                estrategiaPix,
                                estrategiaCartao,
                                estrategiaBoleto
                        )
                );

        assertAll(
                () -> assertSame(
                        estrategiaPix,
                        factory.obter(FormaPagamento.PIX)
                ),
                () -> assertSame(
                        estrategiaCartao,
                        factory.obter(
                                FormaPagamento.CARTAO_CREDITO
                        )
                ),
                () -> assertSame(
                        estrategiaBoleto,
                        factory.obter(FormaPagamento.BOLETO)
                )
        );
    }

    @Test
    void naoDeveObterEstrategiaQuandoFormaPagamentoForNula() {
        EstrategiaPagamentoFactory factory =
                new EstrategiaPagamentoFactory(
                        List.of()
                );

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> factory.obter(null)
        );

        assertEquals(
                "Forma de pagamento é obrigatória",
                excecao.getMessage()
        );
    }

    @Test
    void naoDeveObterFormaPagamentoSemEstrategiaRegistrada() {
        EstrategiaPagamento estrategiaPix = mock(EstrategiaPagamento.class);
        when(estrategiaPix.getFormaPagamento()).thenReturn(FormaPagamento.PIX);

        EstrategiaPagamentoFactory factory =
                new EstrategiaPagamentoFactory(
                        List.of(estrategiaPix)
                );

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> factory.obter(
                        FormaPagamento.BOLETO
                )
        );

        assertEquals(
                "Forma de pagamento não suportada: BOLETO",
                excecao.getMessage()
        );
    }

    @Test
    void naoDeveRegistrarDuasEstrategiasParaMesmaFormaPagamento() {
        EstrategiaPagamento primeiraEstrategiaPix = mock(EstrategiaPagamento.class);
        EstrategiaPagamento segundaEstrategiaPix = mock(EstrategiaPagamento.class);

        when(primeiraEstrategiaPix.getFormaPagamento()).thenReturn(FormaPagamento.PIX);
        when(segundaEstrategiaPix.getFormaPagamento()).thenReturn(FormaPagamento.PIX);

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> new EstrategiaPagamentoFactory(
                        List.of(
                                primeiraEstrategiaPix,
                                segundaEstrategiaPix
                        )
                )
        );

        assertEquals(
                "Já existe uma estratégia para a forma de pagamento PIX",
                excecao.getMessage()
        );
    }
}