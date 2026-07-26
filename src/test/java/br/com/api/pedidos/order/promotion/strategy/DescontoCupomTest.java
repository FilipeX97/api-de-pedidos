package br.com.api.pedidos.order.promotion.strategy;

import br.com.api.pedidos.coupon.entity.Cupom;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DescontoCupomTest {

    private DescontoCupom estrategia;

    @BeforeEach
    void setUp() {
        estrategia = new DescontoCupom();
    }

    @Nested
    class Identificacao {

        @Test
        void devePertencerAoGrupoPromocional() {
            assertEquals(
                    TipoGrupoDesconto.PROMOCIONAL,
                    estrategia.getGrupo()
            );
        }
    }

    @Nested
    class VerificacaoDeAplicabilidade {

        @Test
        void devePermitirAplicarQuandoPedidoTemCupom() {
            Pedido pedido = novoPedidoComValorBruto("500.00");

            pedido.aplicarCupom(
                    novoCupom("DESC20", "0.20")
            );

            assertTrue(
                    estrategia.podeAplicar(pedido)
            );
        }

        @Test
        void naoDeveAplicarQuandoPedidoNaoTemCupom() {
            Pedido pedido = novoPedidoComValorBruto("500.00");

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
        void deveCalcularDescontoDoCupomSobreValorBruto() {
            Pedido pedido = novoPedidoComValorBruto("500.00");

            pedido.aplicarCupom(
                    novoCupom("DESC20", "0.20")
            );

            BigDecimal desconto =
                    estrategia.calcularDesconto(pedido);

            assertBigDecimalEquals(
                    "100.00",
                    desconto
            );
        }

        @Test
        void deveRetornarZeroQuandoPedidoNaoTemCupom() {
            Pedido pedido = novoPedidoComValorBruto("500.00");

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

    private Pedido novoPedidoComValorBruto(String preco) {
        Usuario usuario = novoUsuario();

        Produto produto = new Produto(
                "Mouse",
                "Mouse sem fio",
                new BigDecimal(preco),
                10
        );

        Pedido pedido = new Pedido(usuario);
        pedido.adicionarItem(produto, 1);

        return pedido;
    }

    private Usuario novoUsuario() {
        return new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );
    }

    private Cupom novoCupom(
            String codigo,
            String percentual) {

        return new Cupom(
                codigo,
                new BigDecimal(percentual),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                100
        );
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