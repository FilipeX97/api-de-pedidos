package br.com.api.pedidos.order.promotion.engine;

import br.com.api.pedidos.coupon.entity.Cupom;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.promotion.strategy.DescontoClienteVip;
import br.com.api.pedidos.order.promotion.strategy.DescontoCupom;
import br.com.api.pedidos.order.promotion.strategy.DescontoQuantidade;
import br.com.api.pedidos.order.promotion.strategy.EstrategiaDesconto;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MotorPromocaoTest {

    private MotorPromocao motorPromocao;

    @BeforeEach
    void setUp() {
        List<EstrategiaDesconto> estrategias = List.of(
                new DescontoClienteVip(),
                new DescontoQuantidade(),
                new DescontoCupom()
        );

        motorPromocao = new MotorPromocao(
                estrategias
        );
    }

    @Nested
    class Validacoes {

        @Test
        void naoDeveRecalcularPedidoNulo() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> motorPromocao.recalcular(null)
            );

            assertEquals(
                    "Pedido inválido",
                    excecao.getMessage()
            );
        }
    }

    @Nested
    class PedidoSemDesconto {

        @Test
        void deveManterValoresQuandoNenhumaEstrategiaForAplicavel() {
            Usuario usuario = novoUsuarioComum();

            Pedido pedido = novoPedido(
                    usuario,
                    "100.00",
                    1
            );

            motorPromocao.recalcular(pedido);

            assertAll(
                    () -> assertBigDecimalEquals(
                            "100.00",
                            pedido.getValorBruto()
                    ),
                    () -> assertBigDecimalEquals(
                            "0.00",
                            pedido.getValorDesconto()
                    ),
                    () -> assertBigDecimalEquals(
                            "100.00",
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void deveLimparDescontoAnteriorAntesDeRecalcular() {
            Usuario usuario = novoUsuarioComum();

            Pedido pedido = novoPedido(
                    usuario,
                    "100.00",
                    1
            );

            pedido.aplicarDesconto(
                    new BigDecimal("50.00")
            );

            assertBigDecimalEquals(
                    "50.00",
                    pedido.getValorFinal()
            );

            motorPromocao.recalcular(pedido);

            assertAll(
                    () -> assertBigDecimalEquals(
                            "0.00",
                            pedido.getValorDesconto()
                    ),
                    () -> assertBigDecimalEquals(
                            "100.00",
                            pedido.getValorFinal()
                    )
            );
        }
    }

    @Nested
    class DescontoPromocional {

        @Test
        void deveAplicarDescontoDoCupom() {
            Usuario usuario = novoUsuarioComum();

            Pedido pedido = novoPedido(
                    usuario,
                    "1000.00",
                    1
            );

            pedido.aplicarCupom(
                    novoCupom("DESC20", "0.20")
            );

            motorPromocao.recalcular(pedido);

            assertAll(
                    () -> assertBigDecimalEquals(
                            "1000.00",
                            pedido.getValorBruto()
                    ),
                    () -> assertBigDecimalEquals(
                            "200.00",
                            pedido.getValorDesconto()
                    ),
                    () -> assertBigDecimalEquals(
                            "800.00",
                            pedido.getValorFinal()
                    )
            );
        }
    }

    @Nested
    class DescontoEstrutural {

        @Test
        void deveAplicarDescontoVip() {
            Usuario usuario = novoUsuarioVip();

            Pedido pedido = novoPedido(
                    usuario,
                    "1000.00",
                    1
            );

            motorPromocao.recalcular(pedido);

            assertAll(
                    () -> assertBigDecimalEquals(
                            "150.00",
                            pedido.getValorDesconto()
                    ),
                    () -> assertBigDecimalEquals(
                            "850.00",
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void deveAplicarDescontoPorQuantidade() {
            Usuario usuario = novoUsuarioComum();

            Pedido pedido = novoPedido(
                    usuario,
                    "100.00",
                    10
            );

            motorPromocao.recalcular(pedido);

            assertAll(
                    () -> assertBigDecimalEquals(
                            "1000.00",
                            pedido.getValorBruto()
                    ),
                    () -> assertBigDecimalEquals(
                            "100.00",
                            pedido.getValorDesconto()
                    ),
                    () -> assertBigDecimalEquals(
                            "900.00",
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void deveSomarDescontoVipEQuantidade() {
            Usuario usuario = novoUsuarioVip();

            Pedido pedido = novoPedido(
                    usuario,
                    "100.00",
                    10
            );

            motorPromocao.recalcular(pedido);

            assertAll(
                    () -> assertBigDecimalEquals(
                            "1000.00",
                            pedido.getValorBruto()
                    ),
                    () -> assertBigDecimalEquals(
                            "250.00",
                            pedido.getValorDesconto()
                    ),
                    () -> assertBigDecimalEquals(
                            "750.00",
                            pedido.getValorFinal()
                    )
            );
        }
    }

    @Nested
    class EscolhaDoMaiorGrupo {

        @Test
        void deveEscolherPromocionalQuandoCupomForMaior() {
            Usuario usuario = novoUsuarioVip();

            Pedido pedido = novoPedido(
                    usuario,
                    "100.00",
                    10
            );

            pedido.aplicarCupom(
                    novoCupom("DESC30", "0.30")
            );

            motorPromocao.recalcular(pedido);

            /*
             * Estrutural:
             * VIP        = 15% de 1000 = 150
             * Quantidade = 10% de 1000 = 100
             * Total estrutural         = 250
             *
             * Promocional:
             * Cupom      = 30% de 1000 = 300
             *
             * Deve aplicar 300.
             */
            assertAll(
                    () -> assertBigDecimalEquals(
                            "300.00",
                            pedido.getValorDesconto()
                    ),
                    () -> assertBigDecimalEquals(
                            "700.00",
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void deveEscolherEstruturalQuandoSomaForMaior() {
            Usuario usuario = novoUsuarioVip();

            Pedido pedido = novoPedido(
                    usuario,
                    "100.00",
                    10
            );

            pedido.aplicarCupom(
                    novoCupom("DESC20", "0.20")
            );

            motorPromocao.recalcular(pedido);

            /*
             * Estrutural:
             * VIP        = 150
             * Quantidade = 100
             * Total      = 250
             *
             * Promocional:
             * Cupom      = 200
             *
             * Deve aplicar 250.
             */
            assertAll(
                    () -> assertBigDecimalEquals(
                            "250.00",
                            pedido.getValorDesconto()
                    ),
                    () -> assertBigDecimalEquals(
                            "750.00",
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void deveAplicarMesmoValorQuandoGruposEmpatarem() {
            Usuario usuario = novoUsuarioVip();

            Pedido pedido = novoPedido(
                    usuario,
                    "100.00",
                    10
            );

            pedido.aplicarCupom(
                    novoCupom("DESC25", "0.25")
            );

            motorPromocao.recalcular(pedido);

            assertAll(
                    () -> assertBigDecimalEquals(
                            "250.00",
                            pedido.getValorDesconto()
                    ),
                    () -> assertBigDecimalEquals(
                            "750.00",
                            pedido.getValorFinal()
                    )
            );
        }
    }

    private Usuario novoUsuarioComum() {
        return new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );
    }

    private Usuario novoUsuarioVip() {
        Usuario usuario = novoUsuarioComum();
        usuario.ativarClienteVip();

        return usuario;
    }

    private Pedido novoPedido(
            Usuario usuario,
            String preco,
            int quantidade) {

        Produto produto = new Produto(
                "Mouse",
                "Mouse sem fio",
                new BigDecimal(preco),
                Math.max(quantidade, 20)
        );

        Pedido pedido = new Pedido(usuario);
        pedido.adicionarItem(produto, quantidade);

        return pedido;
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