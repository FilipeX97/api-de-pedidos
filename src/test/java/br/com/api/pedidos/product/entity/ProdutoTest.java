package br.com.api.pedidos.product.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ProdutoTest {

    @Test
    void deveCriarProdutoComDadosValidos() {
        Produto produto = new Produto(
                "Notebook",
                "Notebook Dell",
                BigDecimal.valueOf(3000),
                10
        );

        assertEquals("Notebook", produto.getNome());
        assertEquals(BigDecimal.valueOf(3000), produto.getPreco());
        assertEquals(10, produto.getEstoque());
        assertTrue(produto.getAtivo());
    }

    @Test
    void deveRemoverEstoqueQuandoQuantidadeForValida() {
        Produto produto = new Produto(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                10
        );

        produto.removerEstoque(3);
        assertEquals(7, produto.getEstoque());
    }

    @Test
    void deveLancarErroQuandoEstoqueForInsuficiente() {
        Produto produto = new Produto(
                "Teclado",
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                2
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> produto.removerEstoque(3)
        );
    }

    @Test
    void deveLancarErroQuandoQuantidadeForNegativa() {
        Produto produto = new Produto(
                "Monitor",
                "Monitor 24 polegadas",
                BigDecimal.valueOf(800),
                5
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> produto.removerEstoque(-1)
        );
    }

    @Test
    void deveAdicionarEstoque() {
        Produto produto = new Produto(
                "Monitor",
                "Monitor 24",
                BigDecimal.valueOf(900),
                5
        );

        produto.adicionarEstoque(4);

        assertEquals(9, produto.getEstoque());
    }

    @Test
    void naoDeveCriarProdutoComPrecoInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Produto(
                        "Produto inválido",
                        "Descrição",
                        BigDecimal.ZERO,
                        10
                )
        );
    }
}
