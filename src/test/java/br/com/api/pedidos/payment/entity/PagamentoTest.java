package br.com.api.pedidos.payment.entity;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoTest {

    @Nested
    class CriacaoDoPagamento {

        @Test
        void deveCriarPagamentoPendenteComDadosValidos() {
            Pedido pedido = novoPedidoComItem();

            Pagamento pagamento = new Pagamento(
                    pedido,
                    new BigDecimal("250.00"),
                    FormaPagamento.PIX
            );

            assertAll(
                    () -> assertSame(
                            pedido,
                            pagamento.getPedido()
                    ),
                    () -> assertBigDecimalEquals(
                            "250.00",
                            pagamento.getValor()
                    ),
                    () -> assertEquals(
                            FormaPagamento.PIX,
                            pagamento.getFormaPagamento()
                    ),
                    () -> assertEquals(
                            StatusPagamento.PENDENTE,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertNull(
                            pagamento.getCodigoTransacao()
                    ),
                    () -> assertNull(
                            pagamento.getMensagemRetorno()
                    ),
                    () -> assertNotNull(
                            pagamento.getDataCriacao()
                    ),
                    () -> assertNotNull(
                            pagamento.getDataAtualizacao()
                    ),
                    () -> assertTrue(
                            pagamento.estaPendente()
                    ),
                    () -> assertFalse(
                            pagamento.estaAprovado()
                    ),
                    () -> assertFalse(
                            pagamento.estaRecusado()
                    )
            );
        }

        @Test
        void naoDeveCriarPagamentoSemPedido() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Pagamento(
                            null,
                            new BigDecimal("250.00"),
                            FormaPagamento.PIX
                    )
            );

            assertEquals(
                    "Pedido do pagamento é obrigatório",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarPagamentoComValorNulo() {
            Pedido pedido = novoPedidoComItem();

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Pagamento(
                            pedido,
                            null,
                            FormaPagamento.PIX
                    )
            );

            assertEquals(
                    "Valor do pagamento deve ser maior que zero",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarPagamentoComValorZero() {
            Pedido pedido = novoPedidoComItem();

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Pagamento(
                            pedido,
                            BigDecimal.ZERO,
                            FormaPagamento.PIX
                    )
            );

            assertEquals(
                    "Valor do pagamento deve ser maior que zero",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarPagamentoComValorNegativo() {
            Pedido pedido = novoPedidoComItem();

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Pagamento(
                            pedido,
                            new BigDecimal("-0.01"),
                            FormaPagamento.PIX
                    )
            );

            assertEquals(
                    "Valor do pagamento deve ser maior que zero",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarPagamentoSemFormaDePagamento() {
            Pedido pedido = novoPedidoComItem();

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Pagamento(
                            pedido,
                            new BigDecimal("250.00"),
                            null
                    )
            );

            assertEquals(
                    "Forma de pagamento é obrigatória",
                    excecao.getMessage()
            );
        }
    }

    @Nested
    class AprovacaoDoPagamento {

        @Test
        void deveAprovarPagamento() {
            Pagamento pagamento = novoPagamento(FormaPagamento.CARTAO_CREDITO);
            LocalDateTime dataAtualizacaoAnterior = pagamento.getDataAtualizacao();

            pagamento.aprovar(
                    "CARD-APROVADO-123",
                    "Pagamento autorizado pela operadora"
            );

            assertAll(
                    () -> assertEquals(
                            StatusPagamento.APROVADO,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            "CARD-APROVADO-123",
                            pagamento.getCodigoTransacao()
                    ),
                    () -> assertEquals(
                            "Pagamento autorizado pela operadora",
                            pagamento.getMensagemRetorno()
                    ),
                    () -> assertTrue(
                            pagamento.estaAprovado()
                    ),
                    () -> assertFalse(
                            pagamento.estaPendente()
                    ),
                    () -> assertFalse(
                            pagamento.estaRecusado()
                    ),
                    () -> assertFalse(
                            pagamento.getDataAtualizacao()
                                    .isBefore(dataAtualizacaoAnterior)
                    )
            );
        }

        @Test
        void deveConfirmarPagamentoPendente() {
            Pagamento pagamento = novoPagamento(FormaPagamento.PIX);
            assertTrue(pagamento.estaPendente());

            pagamento.confirmarPagamentoPendente(
                    "PIX-CONFIRMADO-123",
                    "Pagamento PIX confirmado pelo gateway"
            );

            assertAll(
                    () -> assertEquals(
                            StatusPagamento.APROVADO,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            "PIX-CONFIRMADO-123",
                            pagamento.getCodigoTransacao()
                    ),
                    () -> assertEquals(
                            "Pagamento PIX confirmado pelo gateway",
                            pagamento.getMensagemRetorno()
                    ),
                    () -> assertTrue(
                            pagamento.estaAprovado()
                    )
            );
        }

        @Test
        void naoDeveConfirmarPagamentoQueNaoEstaPendente() {
            Pagamento pagamento = novoPagamento(FormaPagamento.CARTAO_CREDITO);

            pagamento.aprovar(
                    "CARD-123",
                    "Pagamento aprovado"
            );

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pagamento.confirmarPagamentoPendente(
                            "CARD-456",
                            "Nova confirmação"
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Somente pagamento pendente pode ser confirmado",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            StatusPagamento.APROVADO,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            "CARD-123",
                            pagamento.getCodigoTransacao()
                    )
            );
        }
    }

    @Nested
    class RecusaDoPagamento {

        @Test
        void deveRecusarPagamento() {
            Pagamento pagamento = novoPagamento(FormaPagamento.CARTAO_CREDITO);
            LocalDateTime dataAtualizacaoAnterior = pagamento.getDataAtualizacao();

            pagamento.recusar(
                    "CARD-RECUSADO-123",
                    "Pagamento recusado pela operadora"
            );

            assertAll(
                    () -> assertEquals(
                            StatusPagamento.RECUSADO,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            "CARD-RECUSADO-123",
                            pagamento.getCodigoTransacao()
                    ),
                    () -> assertEquals(
                            "Pagamento recusado pela operadora",
                            pagamento.getMensagemRetorno()
                    ),
                    () -> assertTrue(
                            pagamento.estaRecusado()
                    ),
                    () -> assertFalse(
                            pagamento.estaAprovado()
                    ),
                    () -> assertFalse(
                            pagamento.estaPendente()
                    ),
                    () -> assertFalse(
                            pagamento.getDataAtualizacao()
                                    .isBefore(dataAtualizacaoAnterior)
                    )
            );
        }
    }

    @Nested
    class PagamentoPendente {

        @Test
        void deveRegistrarDadosDoPagamentoPendente() {
            Pagamento pagamento = novoPagamento(FormaPagamento.BOLETO);

            pagamento.deixarPendente(
                    "BOL-123",
                    "Boleto gerado e aguardando pagamento"
            );

            assertAll(
                    () -> assertEquals(
                            StatusPagamento.PENDENTE,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            "BOL-123",
                            pagamento.getCodigoTransacao()
                    ),
                    () -> assertEquals(
                            "Boleto gerado e aguardando pagamento",
                            pagamento.getMensagemRetorno()
                    ),
                    () -> assertTrue(
                            pagamento.estaPendente()
                    )
            );
        }
    }

    @Nested
    class CancelamentoDoPagamento {

        @Test
        void deveCancelarPagamento() {
            Pagamento pagamento = novoPagamento(
                    FormaPagamento.BOLETO
            );

            LocalDateTime dataAtualizacaoAnterior = pagamento.getDataAtualizacao();
            pagamento.cancelar("Pagamento cancelado pelo cliente");

            assertAll(
                    () -> assertEquals(
                            StatusPagamento.CANCELADO,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            "Pagamento cancelado pelo cliente",
                            pagamento.getMensagemRetorno()
                    ),
                    () -> assertFalse(
                            pagamento.estaPendente()
                    ),
                    () -> assertFalse(
                            pagamento.estaAprovado()
                    ),
                    () -> assertFalse(
                            pagamento.estaRecusado()
                    ),
                    () -> assertFalse(
                            pagamento.getDataAtualizacao()
                                    .isBefore(dataAtualizacaoAnterior)
                    )
            );
        }
    }

    @Nested
    class EstornoDoPagamento {

        @Test
        void deveEstornarPagamentoAprovado() {
            Pagamento pagamento = novoPagamento(
                    FormaPagamento.CARTAO_CREDITO
            );

            pagamento.aprovar(
                    "CARD-APROVADO-123",
                    "Pagamento aprovado"
            );

            LocalDateTime dataAtualizacaoAprovacao = pagamento.getDataAtualizacao();

            pagamento.estornar(
                    "REFUND-123",
                    "Pagamento estornado"
            );

            assertAll(
                    () -> assertEquals(
                            StatusPagamento.ESTORNADO,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            "REFUND-123",
                            pagamento.getCodigoTransacao()
                    ),
                    () -> assertEquals(
                            "Pagamento estornado",
                            pagamento.getMensagemRetorno()
                    ),
                    () -> assertFalse(
                            pagamento.estaAprovado()
                    ),
                    () -> assertFalse(
                            pagamento.estaPendente()
                    ),
                    () -> assertFalse(
                            pagamento.estaRecusado()
                    ),
                    () -> assertFalse(
                            pagamento.getDataAtualizacao()
                                    .isBefore(dataAtualizacaoAprovacao)
                    )
            );
        }

        @Test
        void naoDeveEstornarPagamentoPendente() {
            Pagamento pagamento = novoPagamento(
                    FormaPagamento.PIX
            );

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pagamento.estornar(
                            "REFUND-123",
                            "Tentativa de estorno"
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Somente pagamento aprovado pode ser estornado",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            StatusPagamento.PENDENTE,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertNull(
                            pagamento.getCodigoTransacao()
                    )
            );
        }

        @Test
        void naoDeveEstornarPagamentoRecusado() {
            Pagamento pagamento = novoPagamento(
                    FormaPagamento.CARTAO_CREDITO
            );

            pagamento.recusar(
                    "CARD-RECUSADO-123",
                    "Pagamento recusado"
            );

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pagamento.estornar(
                            "REFUND-123",
                            "Tentativa de estorno"
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Somente pagamento aprovado pode ser estornado",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            StatusPagamento.RECUSADO,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            "CARD-RECUSADO-123",
                            pagamento.getCodigoTransacao()
                    ),
                    () -> assertEquals(
                            "Pagamento recusado",
                            pagamento.getMensagemRetorno()
                    )
            );
        }

        @Test
        void naoDeveEstornarPagamentoCancelado() {
            Pagamento pagamento = novoPagamento(
                    FormaPagamento.BOLETO
            );

            pagamento.cancelar(
                    "Pagamento cancelado"
            );

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pagamento.estornar(
                            "REFUND-123",
                            "Tentativa de estorno"
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Somente pagamento aprovado pode ser estornado",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            StatusPagamento.CANCELADO,
                            pagamento.getStatusPagamento()
                    )
            );
        }
    }

    private Pagamento novoPagamento(
            FormaPagamento formaPagamento) {

        return new Pagamento(
                novoPedidoComItem(),
                new BigDecimal("250.00"),
                formaPagamento
        );
    }

    private Pedido novoPedidoComItem() {
        Usuario usuario = new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );

        Produto produto = new Produto(
                "Mouse",
                "Mouse sem fio",
                new BigDecimal("250.00"),
                10
        );

        Pedido pedido = new Pedido(usuario);
        pedido.adicionarItem(produto, 1);

        return pedido;
    }

    private void assertBigDecimalEquals(
            String esperado,
            BigDecimal atual) {

        assertEquals(
                0,
                new BigDecimal(esperado).compareTo(atual)
        );
    }
}