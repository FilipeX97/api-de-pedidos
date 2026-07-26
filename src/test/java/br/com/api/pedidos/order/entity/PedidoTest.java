package br.com.api.pedidos.order.entity;

import br.com.api.pedidos.coupon.entity.Cupom;
import br.com.api.pedidos.order.state.EstadoCancelamentoSolicitado;
import br.com.api.pedidos.order.state.EstadoCriado;
import br.com.api.pedidos.order.state.EstadoPago;
import br.com.api.pedidos.order.state.StatusPedido;
import br.com.api.pedidos.order.valueobject.ItemPedidoId;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    @Nested
    class CriacaoDoPedido {

        @Test
        void deveCriarPedidoComUsuarioValido() {
            Usuario usuario = novoUsuario();

            Pedido pedido = new Pedido(usuario);

            assertAll(
                    () -> assertSame(
                            usuario,
                            pedido.getUsuario()
                    ),
                    () -> assertEquals(
                            StatusPedido.CRIADO,
                            pedido.getStatus()
                    ),
                    () -> assertNotNull(
                            pedido.getDataCriacao()
                    ),
                    () -> assertTrue(
                            pedido.estaVazio()
                    ),
                    () -> assertTrue(
                            pedido.getItens().isEmpty()
                    ),
                    () -> assertEquals(
                            BigDecimal.ZERO,
                            pedido.getValorBruto()
                    ),
                    () -> assertEquals(
                            BigDecimal.ZERO,
                            pedido.getValorDesconto()
                    ),
                    () -> assertEquals(
                            BigDecimal.ZERO,
                            pedido.getValorFinal()
                    ),
                    () -> assertNull(
                            pedido.getCupom()
                    ),
                    () -> assertFalse(
                            pedido.possuiCupom()
                    )
            );
        }

        @Test
        void naoDeveCriarPedidoSemUsuario() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Pedido(null)
            );

            assertEquals(
                    "Usuário é obrigatório",
                    excecao.getMessage()
            );
        }
    }

    @Nested
    class GerenciamentoDosItens {

        @Test
        void deveAdicionarItemRecalcularValoresEReduzirEstoque() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            pedido.adicionarItem(produto, 2);

            assertAll(
                    () -> assertFalse(
                            pedido.estaVazio()
                    ),
                    () -> assertEquals(
                            1,
                            pedido.getItens().size()
                    ),
                    () -> assertSame(
                            produto,
                            pedido.getItens().get(0).getProduto()
                    ),
                    () -> assertEquals(
                            2,
                            pedido.getItens().get(0).getQuantidade()
                    ),
                    () -> assertEquals(
                            8,
                            produto.getEstoque()
                    ),
                    () -> assertEquals(
                            new BigDecimal("200.00"),
                            pedido.getValorBruto()
                    ),
                    () -> assertEquals(
                            BigDecimal.ZERO,
                            pedido.getValorDesconto()
                    ),
                    () -> assertEquals(
                            new BigDecimal("200.00"),
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void naoDeveAdicionarProdutoNulo() {
            Pedido pedido = novoPedido();

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pedido.adicionarItem(null, 1)
            );

            assertAll(
                    () -> assertEquals(
                            "Produto é obrigatório",
                            excecao.getMessage()
                    ),
                    () -> assertTrue(
                            pedido.estaVazio()
                    ),
                    () -> assertEquals(
                            BigDecimal.ZERO,
                            pedido.getValorBruto()
                    )
            );
        }

        @Test
        void naoDeveAdicionarItemComQuantidadeNula() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pedido.adicionarItem(
                            produto,
                            null
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Quantidade inválida",
                            excecao.getMessage()
                    ),
                    () -> assertTrue(
                            pedido.estaVazio()
                    ),
                    () -> assertEquals(
                            10,
                            produto.getEstoque()
                    )
            );
        }

        @Test
        void naoDeveAdicionarItemComQuantidadeZero() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pedido.adicionarItem(
                            produto,
                            0
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Quantidade inválida",
                            excecao.getMessage()
                    ),
                    () -> assertTrue(
                            pedido.estaVazio()
                    ),
                    () -> assertEquals(
                            10,
                            produto.getEstoque()
                    )
            );
        }

        @Test
        void naoDeveAdicionarItemComQuantidadeNegativa() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pedido.adicionarItem(
                            produto,
                            -1
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Quantidade inválida",
                            excecao.getMessage()
                    ),
                    () -> assertTrue(
                            pedido.estaVazio()
                    ),
                    () -> assertEquals(
                            10,
                            produto.getEstoque()
                    )
            );
        }

        @Test
        void naoDeveAdicionarItemSemEstoqueSuficiente() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    1
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pedido.adicionarItem(
                            produto,
                            2
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Estoque insuficiente",
                            excecao.getMessage()
                    ),
                    () -> assertTrue(
                            pedido.estaVazio()
                    ),
                    () -> assertEquals(
                            1,
                            produto.getEstoque()
                    ),
                    () -> assertEquals(
                            BigDecimal.ZERO,
                            pedido.getValorBruto()
                    )
            );
        }

        @Test
        void deveSomarQuantidadeQuandoAdicionarMesmoProduto() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            pedido.adicionarItem(produto, 2);
            pedido.adicionarItem(produto, 3);

            assertAll(
                    () -> assertEquals(
                            1,
                            pedido.getItens().size()
                    ),
                    () -> assertEquals(
                            5,
                            pedido.getItens().get(0).getQuantidade()
                    ),
                    () -> assertEquals(
                            5,
                            produto.getEstoque()
                    ),
                    () -> assertEquals(
                            new BigDecimal("500.00"),
                            pedido.getValorBruto()
                    ),
                    () -> assertEquals(
                            new BigDecimal("500.00"),
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void deveAdicionarProdutosDiferentesECalcularValorTotal() {
            Pedido pedido = novoPedido();

            Produto mouse = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            Produto teclado = novoProduto(
                    2L,
                    "Teclado",
                    "250.00",
                    10
            );

            pedido.adicionarItem(mouse, 2);
            pedido.adicionarItem(teclado, 1);

            assertAll(
                    () -> assertEquals(
                            2,
                            pedido.getItens().size()
                    ),
                    () -> assertEquals(
                            new BigDecimal("450.00"),
                            pedido.getValorBruto()
                    ),
                    () -> assertEquals(
                            new BigDecimal("450.00"),
                            pedido.getValorFinal()
                    ),
                    () -> assertEquals(
                            8,
                            mouse.getEstoque()
                    ),
                    () -> assertEquals(
                            9,
                            teclado.getEstoque()
                    )
            );
        }

        @Test
        void deveAlterarQuantidadeDoItemERecalcularPedido() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            ItemPedido item = adicionarItemComId(
                    pedido,
                    produto,
                    2,
                    1L
            );

            pedido.aplicarDesconto(
                    new BigDecimal("20.00")
            );

            pedido.alterarQuantidadeDoItem(
                    new ItemPedidoId(item.getId()),
                    4
            );

            assertAll(
                    () -> assertEquals(
                            4,
                            item.getQuantidade()
                    ),
                    () -> assertEquals(
                            6,
                            produto.getEstoque()
                    ),
                    () -> assertEquals(
                            new BigDecimal("400.00"),
                            pedido.getValorBruto()
                    ),
                    () -> assertEquals(
                            new BigDecimal("20.00"),
                            pedido.getValorDesconto()
                    ),
                    () -> assertEquals(
                            new BigDecimal("380.00"),
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void naoDeveAlterarItemQueNaoPertenceAoPedido() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            adicionarItemComId(
                    pedido,
                    produto,
                    2,
                    1L
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pedido.alterarQuantidadeDoItem(
                            new ItemPedidoId(999L),
                            5
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Item não encontrado no pedido",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            2,
                            pedido.getItens().get(0).getQuantidade()
                    ),
                    () -> assertEquals(
                            8,
                            produto.getEstoque()
                    ),
                    () -> assertEquals(
                            new BigDecimal("200.00"),
                            pedido.getValorBruto()
                    )
            );
        }

        @Test
        void deveRemoverItemDevolverEstoqueERecalcularPedido() {
            Pedido pedido = novoPedido();

            Produto mouse = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            Produto teclado = novoProduto(
                    2L,
                    "Teclado",
                    "250.00",
                    10
            );

            ItemPedido itemMouse = adicionarItemComId(
                    pedido,
                    mouse,
                    2,
                    1L
            );

            adicionarItemComId(
                    pedido,
                    teclado,
                    1,
                    2L
            );

            assertEquals(
                    new BigDecimal("450.00"),
                    pedido.getValorBruto()
            );

            pedido.removerItem(
                    new ItemPedidoId(itemMouse.getId())
            );

            assertAll(
                    () -> assertEquals(
                            1,
                            pedido.getItens().size()
                    ),
                    () -> assertSame(
                            teclado,
                            pedido.getItens().get(0).getProduto()
                    ),
                    () -> assertEquals(
                            10,
                            mouse.getEstoque()
                    ),
                    () -> assertEquals(
                            9,
                            teclado.getEstoque()
                    ),
                    () -> assertEquals(
                            new BigDecimal("250.00"),
                            pedido.getValorBruto()
                    ),
                    () -> assertEquals(
                            new BigDecimal("250.00"),
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void naoDeveRemoverItemQueNaoPertenceAoPedido() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            adicionarItemComId(
                    pedido,
                    produto,
                    2,
                    1L
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pedido.removerItem(
                            new ItemPedidoId(999L)
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Item não encontrado no pedido",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            1,
                            pedido.getItens().size()
                    ),
                    () -> assertEquals(
                            8,
                            produto.getEstoque()
                    ),
                    () -> assertEquals(
                            new BigDecimal("200.00"),
                            pedido.getValorBruto()
                    )
            );
        }
    }

    @Nested
    class DescontosECupons {

        @Test
        void deveAplicarDescontoERecalcularValorFinal() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            pedido.adicionarItem(produto, 2);

            pedido.aplicarDesconto(
                    new BigDecimal("30.00")
            );

            assertAll(
                    () -> assertEquals(
                            new BigDecimal("200.00"),
                            pedido.getValorBruto()
                    ),
                    () -> assertEquals(
                            new BigDecimal("30.00"),
                            pedido.getValorDesconto()
                    ),
                    () -> assertEquals(
                            new BigDecimal("170.00"),
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void deveConsiderarDescontoNuloComoZero() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            pedido.adicionarItem(produto, 2);
            pedido.aplicarDesconto(
                    new BigDecimal("20.00")
            );

            pedido.aplicarDesconto(null);

            assertAll(
                    () -> assertEquals(
                            BigDecimal.ZERO,
                            pedido.getValorDesconto()
                    ),
                    () -> assertEquals(
                            new BigDecimal("200.00"),
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void naoDeveAplicarDescontoNegativo() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            pedido.adicionarItem(produto, 2);

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pedido.aplicarDesconto(
                            new BigDecimal("-10.00")
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Desconto inválido",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            BigDecimal.ZERO,
                            pedido.getValorDesconto()
                    ),
                    () -> assertEquals(
                            new BigDecimal("200.00"),
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void deveLimparDescontos() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            pedido.adicionarItem(produto, 2);
            pedido.aplicarDesconto(
                    new BigDecimal("30.00")
            );

            pedido.limparDescontos();

            assertAll(
                    () -> assertEquals(
                            BigDecimal.ZERO,
                            pedido.getValorDesconto()
                    ),
                    () -> assertEquals(
                            new BigDecimal("200.00"),
                            pedido.getValorFinal()
                    )
            );
        }

        @Test
        void deveAplicarCupomValidoAoPedido() {
            Pedido pedido = novoPedido();
            Cupom cupom = novoCupomValido();

            pedido.aplicarCupom(cupom);

            assertAll(
                    () -> assertTrue(
                            pedido.possuiCupom()
                    ),
                    () -> assertSame(
                            cupom,
                            pedido.getCupom()
                    ),
                    () -> assertEquals(
                            "DESC10",
                            pedido.getCupom().getCodigo()
                    )
            );
        }

        @Test
        void naoDeveAplicarCupomNulo() {
            Pedido pedido = novoPedido();

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pedido.aplicarCupom(null)
            );

            assertAll(
                    () -> assertEquals(
                            "Cupom inválido",
                            excecao.getMessage()
                    ),
                    () -> assertFalse(
                            pedido.possuiCupom()
                    ),
                    () -> assertNull(
                            pedido.getCupom()
                    )
            );
        }
    }

    @Nested
    class TransicoesEEstoque {

        @Test
        void devePagarPedidoComItem() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            pedido.adicionarItem(produto, 1);

            pedido.pagar(new EstadoCriado());

            assertEquals(
                    StatusPedido.PAGO,
                    pedido.getStatus()
            );
        }

        @Test
        void naoDevePagarPedidoSemItens() {
            Pedido pedido = novoPedido();

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pedido.pagar(
                            new EstadoCriado()
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Não é possível iniciar pagamento de um pedido sem itens.",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            StatusPedido.CRIADO,
                            pedido.getStatus()
                    )
            );
        }

        @Test
        void deveDeixarPedidoAguardandoPagamento() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            pedido.adicionarItem(produto, 1);

            pedido.aguardarPagamento(
                    new EstadoCriado()
            );

            assertEquals(
                    StatusPedido.AGUARDANDO_PAGAMENTO,
                    pedido.getStatus()
            );
        }

        @Test
        void deveCancelarPedidoCriadoEDevolverEstoque() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            pedido.adicionarItem(produto, 2);

            assertEquals(
                    8,
                    produto.getEstoque()
            );

            pedido.cancelar(
                    new EstadoCriado()
            );

            assertAll(
                    () -> assertEquals(
                            StatusPedido.CANCELADO,
                            pedido.getStatus()
                    ),
                    () -> assertEquals(
                            10,
                            produto.getEstoque()
                    )
            );
        }

        @Test
        void deveSolicitarCancelamentoDePedidoPagoSemDevolverEstoque() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            pedido.adicionarItem(produto, 2);
            pedido.pagar(new EstadoCriado());

            pedido.cancelar(new EstadoPago());

            assertAll(
                    () -> assertEquals(
                            StatusPedido.CANCELAMENTO_SOLICITADO,
                            pedido.getStatus()
                    ),
                    () -> assertEquals(
                            8,
                            produto.getEstoque(),
                            "O estoque só deve voltar após o estorno"
                    )
            );
        }

        @Test
        void deveEstornarPedidoEDevolverEstoque() {
            Pedido pedido = novoPedido();

            Produto produto = novoProduto(
                    1L,
                    "Mouse",
                    "100.00",
                    10
            );

            pedido.adicionarItem(produto, 2);
            pedido.pagar(new EstadoCriado());
            pedido.cancelar(new EstadoPago());

            pedido.estornar(
                    new EstadoCancelamentoSolicitado()
            );

            assertAll(
                    () -> assertEquals(
                            StatusPedido.ESTORNADO,
                            pedido.getStatus()
                    ),
                    () -> assertEquals(
                            10,
                            produto.getEstoque()
                    )
            );
        }
    }

    private Pedido novoPedido() {
        return new Pedido(novoUsuario());
    }

    private Usuario novoUsuario() {
        return new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );
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

    private ItemPedido adicionarItemComId(
            Pedido pedido,
            Produto produto,
            Integer quantidade,
            Long idItem) {
        pedido.adicionarItem(produto, quantidade);

        ItemPedido itemAdicionado = pedido
                .getItens()
                .get(pedido.getItens().size() - 1);

        ReflectionTestUtils.setField(
                itemAdicionado,
                "id",
                idItem
        );

        return itemAdicionado;
    }

    private Cupom novoCupomValido() {
        return new Cupom(
                "DESC10",
                new BigDecimal("0.10"),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                100
        );
    }
}