package br.com.api.pedidos.order.promotion;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.promotion.strategy.DescontoQuantidade;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DescontoQuantidadeTest {

    @Test
    void deveAplicarDescontoQuandoItemTemQuantidadeMinima() {
        Usuario usuario = new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );

        Produto produto = new Produto(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                20
        );

        Pedido pedido = new Pedido(usuario);
        pedido.adicionarItem(produto, 10);

        DescontoQuantidade descontoQuantidade = new DescontoQuantidade();
        BigDecimal desconto = descontoQuantidade.calcularDesconto(pedido);

        assertEquals(0, BigDecimal.valueOf(100).compareTo(desconto));
    }

    @Test
    void naoDeveAplicarDescontoQuandoQuantidadeForMenorQueMinima() {
        Usuario usuario = new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );

        Produto produto = new Produto(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                20
        );

        Pedido pedido = new Pedido(usuario);
        pedido.adicionarItem(produto, 9);

        DescontoQuantidade descontoQuantidade = new DescontoQuantidade();
        BigDecimal desconto = descontoQuantidade.calcularDesconto(pedido);

        assertEquals(0, BigDecimal.ZERO.compareTo(desconto));
    }
}
