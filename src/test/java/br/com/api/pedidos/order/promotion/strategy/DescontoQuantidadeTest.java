package br.com.api.pedidos.order.promotion.strategy;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DescontoQuantidadeTest {

    private DescontoQuantidade estrategia;

    @BeforeEach
    void setUp() {
        estrategia = new DescontoQuantidade();
    }

    @Nested
    class Identificacao {

        @Test
        void devePertencerAoGrupoEstrutural() {
            assertEquals(
                    TipoGrupoDesconto.ESTRUTURAL,
                    estrategia.getGrupo()
            );
        }
    }

    @Nested
    class VerificacaoDeAplicabilidade {

        @Test
        void devePermitirAplicarQuandoItemTemDezUnidades() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    20
            );

            pedido.adicionarItem(produto, 10);

            assertTrue(
                    estrategia.podeAplicar(pedido)
            );
        }

        @Test
        void devePermitirAplicarQuandoItemTemMaisDeDezUnidades() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    20
            );

            pedido.adicionarItem(produto, 11);

            assertTrue(
                    estrategia.podeAplicar(pedido)
            );
        }

        @Test
        void naoDeveAplicarQuandoQuantidadeForNove() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    20
            );

            pedido.adicionarItem(produto, 9);

            assertFalse(
                    estrategia.podeAplicar(pedido)
            );
        }

        @Test
        void naoDeveAplicarEmPedidoVazio() {
            Pedido pedido = novoPedido();

            assertFalse(
                    estrategia.podeAplicar(pedido)
            );
        }

        @Test
        void naoDeveAplicarQuandoPedidoForNulo() {
            assertFalse(
                    estrategia.podeAplicar(null)
            );
        }
    }

    @Nested
    class CalculoDoDesconto {

        @Test
        void deveCalcularDezPorCentoDoItemElegivel() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    20
            );

            pedido.adicionarItem(produto, 10);

            BigDecimal desconto =
                    estrategia.calcularDesconto(pedido);

            assertBigDecimalEquals(
                    "100.00",
                    desconto
            );
        }

        @Test
        void deveCalcularDescontoSomenteSobreItensElegiveis() {
            Pedido pedido = novoPedido();

            Produto mouse = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    20
            );

            Produto teclado = novoProduto(
                    2L,
                    "Teclado",
                    "500.00",
                    20
            );

            pedido.adicionarItem(mouse, 10);
            pedido.adicionarItem(teclado, 2);

            BigDecimal desconto =
                    estrategia.calcularDesconto(pedido);

            assertAll(
                    () -> assertBigDecimalEquals(
                            "2000.00",
                            pedido.getValorBruto()
                    ),
                    () -> assertBigDecimalEquals(
                            "100.00",
                            desconto
                    )
            );
        }

        @Test
        void deveSomarDescontoDeTodosOsItensElegiveis() {
            Pedido pedido = novoPedido();

            Produto mouse = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    20
            );

            Produto cabo = novoProduto(
                    2L,
                    "Cabo",
                    "20.00",
                    20
            );

            pedido.adicionarItem(mouse, 10);
            pedido.adicionarItem(cabo, 10);

            BigDecimal desconto =
                    estrategia.calcularDesconto(pedido);

            assertAll(
                    () -> assertBigDecimalEquals(
                            "1200.00",
                            pedido.getValorBruto()
                    ),
                    () -> assertBigDecimalEquals(
                            "120.00",
                            desconto
                    )
            );
        }

        @Test
        void deveRetornarZeroQuandoNenhumItemForElegivel() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    20
            );

            pedido.adicionarItem(produto, 9);

            BigDecimal desconto =
                    estrategia.calcularDesconto(pedido);

            assertBigDecimalEquals(
                    "0.00",
                    desconto
            );
        }

        @Test
        void deveRetornarZeroQuandoPedidoForNulo() {
            BigDecimal desconto =
                    estrategia.calcularDesconto(null);

            assertBigDecimalEquals(
                    "0.00",
                    desconto
            );
        }
    }

    private Pedido novoPedido() {
        Usuario usuario = new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );

        return new Pedido(usuario);
    }

    private Produto novoProduto(
            Long id,
            String nome,
            String preco,
            Integer estoque) {

        Produto produto = new Produto(
                nome,
                "Descrição do produto " + nome,
                new BigDecimal(preco),
                estoque
        );

        ReflectionTestUtils.setField(
                produto,
                "id",
                id
        );

        return produto;
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