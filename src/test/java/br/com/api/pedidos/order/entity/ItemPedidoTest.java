package br.com.api.pedidos.order.entity;

import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    @Nested
    class CriacaoDoItem {

        @Test
        void deveCriarItemEReservarEstoqueDoProduto() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("100.00"),
                    10
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    2
            );

            assertAll(
                    () -> assertSame(
                            produto,
                            item.getProduto()
                    ),
                    () -> assertEquals(
                            2,
                            item.getQuantidade()
                    ),
                    () -> assertEquals(
                            new BigDecimal("100.00"),
                            item.getPrecoUnitario()
                    ),
                    () -> assertEquals(
                            8,
                            produto.getEstoque()
                    )
            );
        }

        @Test
        void deveCalcularSubtotalCorretamente() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("99.90"),
                    10
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    3
            );

            assertEquals(
                    new BigDecimal("299.70"),
                    item.getSubtotal()
            );
        }

        @Test
        void deveManterPrecoUnitarioRegistradoNaCriacao() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("100.00"),
                    10
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    2
            );

            produto.alterarPreco(new BigDecimal("150.00"));

            assertAll(
                    () -> assertEquals(
                            new BigDecimal("150.00"),
                            produto.getPreco()
                    ),
                    () -> assertEquals(
                            new BigDecimal("100.00"),
                            item.getPrecoUnitario()
                    ),
                    () -> assertEquals(
                            new BigDecimal("200.00"),
                            item.getSubtotal()
                    )
            );
        }

        @Test
        void naoDeveCriarItemComQuantidadeNula() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("100.00"),
                    10
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new ItemPedido(
                            pedido,
                            produto,
                            null
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Quantidade deve ser maior que zero",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            10,
                            produto.getEstoque()
                    )
            );
        }

        @Test
        void naoDeveCriarItemComQuantidadeZero() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("100.00"),
                    10
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new ItemPedido(
                            pedido,
                            produto,
                            0
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Quantidade deve ser maior que zero",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            10,
                            produto.getEstoque()
                    )
            );
        }

        @Test
        void naoDeveCriarItemComQuantidadeNegativa() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("100.00"),
                    10
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new ItemPedido(
                            pedido,
                            produto,
                            -1
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Quantidade deve ser maior que zero",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            10,
                            produto.getEstoque()
                    )
            );
        }

        @Test
        void naoDeveCriarItemQuandoEstoqueForInsuficiente() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("100.00"),
                    2
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new ItemPedido(
                            pedido,
                            produto,
                            3
                    )
            );

            assertAll(
                    () -> assertEquals(
                            "Estoque insuficiente",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            2,
                            produto.getEstoque(),
                            "O estoque não pode mudar quando a operação falha"
                    )
            );
        }
    }

    @Nested
    class AlteracaoDaQuantidade {

        @Test
        void deveAumentarQuantidadeEReduzirEstoque() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("50.00"),
                    10
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    2
            );

            item.alterarQuantidade(5);

            assertAll(
                    () -> assertEquals(
                            5,
                            item.getQuantidade()
                    ),
                    () -> assertEquals(
                            5,
                            produto.getEstoque()
                    ),
                    () -> assertEquals(
                            new BigDecimal("250.00"),
                            item.getSubtotal()
                    )
            );
        }

        @Test
        void deveSomarQuantidadeAoItemExistente() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("50.00"),
                    10
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    2
            );

            item.adicionarQuantidade(3);

            assertAll(
                    () -> assertEquals(
                            5,
                            item.getQuantidade()
                    ),
                    () -> assertEquals(
                            5,
                            produto.getEstoque()
                    ),
                    () -> assertEquals(
                            new BigDecimal("250.00"),
                            item.getSubtotal()
                    )
            );
        }

        @Test
        void deveDiminuirQuantidadeEDevolverEstoque() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("50.00"),
                    10
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    5
            );

            assertEquals(
                    5,
                    produto.getEstoque()
            );

            item.alterarQuantidade(2);

            assertAll(
                    () -> assertEquals(
                            2,
                            item.getQuantidade()
                    ),
                    () -> assertEquals(
                            8,
                            produto.getEstoque()
                    ),
                    () -> assertEquals(
                            new BigDecimal("100.00"),
                            item.getSubtotal()
                    )
            );
        }

        @Test
        void naoDeveAlterarEstoqueQuandoQuantidadeForIgual() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("50.00"),
                    10
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    2
            );

            item.alterarQuantidade(2);

            assertAll(
                    () -> assertEquals(
                            2,
                            item.getQuantidade()
                    ),
                    () -> assertEquals(
                            8,
                            produto.getEstoque()
                    )
            );
        }

        @Test
        void naoDeveAlterarQuantidadeParaZero() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("50.00"),
                    10
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    2
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> item.alterarQuantidade(0)
            );

            assertAll(
                    () -> assertEquals(
                            "Quantidade deve ser maior que zero",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            2,
                            item.getQuantidade()
                    ),
                    () -> assertEquals(
                            8,
                            produto.getEstoque()
                    )
            );
        }

        @Test
        void naoDeveAlterarQuantidadeParaValorNegativo() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("50.00"),
                    10
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    2
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> item.alterarQuantidade(-1)
            );

            assertAll(
                    () -> assertEquals(
                            "Quantidade deve ser maior que zero",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            2,
                            item.getQuantidade()
                    ),
                    () -> assertEquals(
                            8,
                            produto.getEstoque()
                    )
            );
        }

        @Test
        void naoDeveAlterarQuantidadeParaNulo() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("50.00"),
                    10
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    2
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> item.alterarQuantidade(null)
            );

            assertAll(
                    () -> assertEquals(
                            "Quantidade deve ser maior que zero",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            2,
                            item.getQuantidade()
                    ),
                    () -> assertEquals(
                            8,
                            produto.getEstoque()
                    )
            );
        }

        @Test
        void naoDeveAumentarQuantidadeSemEstoqueSuficiente() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("50.00"),
                    5
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    3
            );

            assertEquals(
                    2,
                    produto.getEstoque()
            );

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> item.alterarQuantidade(6)
            );

            assertAll(
                    () -> assertEquals(
                            "Estoque insuficiente",
                            excecao.getMessage()
                    ),
                    () -> assertEquals(
                            3,
                            item.getQuantidade(),
                            "A quantidade anterior deve ser mantida"
                    ),
                    () -> assertEquals(
                            2,
                            produto.getEstoque(),
                            "O estoque não deve ser alterado quando a operação falha"
                    )
            );
        }
    }

    @Nested
    class DevolucaoDoEstoque {

        @Test
        void deveDevolverEstoqueReservado() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("100.00"),
                    10
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    3
            );

            assertEquals(
                    7,
                    produto.getEstoque()
            );

            item.devolverEstoqueReservado();

            assertAll(
                    () -> assertEquals(
                            10,
                            produto.getEstoque()
                    ),
                    () -> assertEquals(
                            3,
                            item.getQuantidade(),
                            "A devolução não deve alterar a quantidade registrada"
                    )
            );
        }

        @Test
        void deveRemoverItemCompletamenteEDevolverEstoque() {
            Pedido pedido = novoPedido();
            Produto produto = novoProduto(
                    new BigDecimal("100.00"),
                    10
            );

            ItemPedido item = new ItemPedido(
                    pedido,
                    produto,
                    3
            );

            assertEquals(
                    7,
                    produto.getEstoque()
            );

            item.removerCompletamente();

            assertAll(
                    () -> assertEquals(
                            10,
                            produto.getEstoque()
                    ),
                    () -> assertEquals(
                            0,
                            item.getQuantidade()
                    ),
                    () -> assertEquals(
                            new BigDecimal("0.00"),
                            item.getSubtotal()
                    )
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
            BigDecimal preco,
            Integer estoque) {
        return new Produto(
                "Mouse",
                "Mouse sem fio",
                preco,
                estoque
        );
    }
}