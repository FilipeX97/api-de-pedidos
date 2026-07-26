package br.com.api.pedidos.order.promotion.strategy;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DescontoClienteVipTest {

    private DescontoClienteVip estrategia;

    @BeforeEach
    void setUp() {
        estrategia = new DescontoClienteVip();
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
        void devePermitirAplicarParaClienteVip() {
            Usuario usuario = novoUsuario();
            usuario.ativarClienteVip();

            Pedido pedido = novoPedido(
                    usuario,
                    "200.00",
                    1
            );

            assertTrue(
                    estrategia.podeAplicar(pedido)
            );
        }

        @Test
        void naoDeveAplicarParaClienteComum() {
            Usuario usuario = novoUsuario();

            Pedido pedido = novoPedido(
                    usuario,
                    "200.00",
                    1
            );

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
        void deveCalcularQuinzePorCentoSobreValorBruto() {
            Usuario usuario = novoUsuario();
            usuario.ativarClienteVip();

            Pedido pedido = novoPedido(
                    usuario,
                    "200.00",
                    2
            );

            BigDecimal desconto =
                    estrategia.calcularDesconto(pedido);

            assertBigDecimalEquals(
                    "60.00",
                    desconto
            );
        }

        @Test
        void deveRetornarZeroParaClienteComum() {
            Usuario usuario = novoUsuario();

            Pedido pedido = novoPedido(
                    usuario,
                    "200.00",
                    2
            );

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

    private Usuario novoUsuario() {
        return new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );
    }

    private Pedido novoPedido(
            Usuario usuario,
            String preco,
            int quantidade) {

        Produto produto = new Produto(
                "Teclado",
                "Teclado mecânico",
                new BigDecimal(preco),
                20
        );

        Pedido pedido = new Pedido(usuario);
        pedido.adicionarItem(produto, quantidade);

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
