package br.com.api.pedidos.order.entity;

import br.com.api.pedidos.order.state.EstadoCancelamentoSolicitado;
import br.com.api.pedidos.order.state.EstadoCriado;
import br.com.api.pedidos.order.state.EstadoPago;
import br.com.api.pedidos.order.state.StatusPedido;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PedidoTest {

    @Test
    void deveCriarPedidoComStatusCriado() {
        Usuario usuario = novoUsuario();

        Pedido pedido = new Pedido(usuario);

        assertEquals(StatusPedido.CRIADO, pedido.getStatus());
        assertTrue(pedido.estaVazio());
        assertEquals(BigDecimal.ZERO, pedido.getValorBruto());
        assertEquals(BigDecimal.ZERO, pedido.getValorFinal());
    }

    @Test
    void deveAdicionarItemERemoverEstoqueDoProduto() {
        Usuario usuario = novoUsuario();
        Produto produto = novoProdutoComEstoque(10);
        Pedido pedido = new Pedido(usuario);

        pedido.adicionarItem(produto, 2);

        assertEquals(8, produto.getEstoque());
        assertEquals(1, pedido.getItens().size());
        assertEquals(BigDecimal.valueOf(200), pedido.getValorBruto());
    }

    @Test
    void deveSomarQuantidadeQuandoAdicionarMesmoProduto() {
        Usuario usuario = novoUsuario();
        Produto produto = novoProdutoComEstoque(10);
        Pedido pedido = new Pedido(usuario);

        pedido.adicionarItem(produto, 2);
        pedido.adicionarItem(produto, 3);

        assertEquals(5, pedido.getItens().get(0).getQuantidade());
        assertEquals(5, produto.getEstoque());
    }

    @Test
    void devePagarPedidoComItem() {
        Usuario usuario = novoUsuario();
        Produto produto = novoProdutoComEstoque(10);
        Pedido pedido = new Pedido(usuario);

        pedido.adicionarItem(produto, 1);
        pedido.pagar(new EstadoCriado());

        assertEquals(StatusPedido.PAGO, pedido.getStatus());
    }

    @Test
    void naoDevePagarPedidoSemItens() {
        Usuario usuario = novoUsuario();
        Pedido pedido = new Pedido(usuario);

        assertThrows(
                IllegalStateException.class,
                () -> pedido.pagar(new EstadoCriado())
        );
    }

    @Test
    void deveCancelarPedidoCriadoEDevolverEstoque() {
        Usuario usuario = novoUsuario();
        Produto produto = novoProdutoComEstoque(10);
        Pedido pedido = new Pedido(usuario);

        pedido.adicionarItem(produto, 2);
        assertEquals(8, produto.getEstoque());

        pedido.cancelar(new EstadoCriado());

        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        assertEquals(10, produto.getEstoque());
    }

    @Test
    void deveSolicitarCancelamentoQuandoPedidoEstaPago() {
        Usuario usuario = novoUsuario();
        Produto produto = novoProdutoComEstoque(10);
        Pedido pedido = new Pedido(usuario);

        pedido.adicionarItem(produto, 2);
        pedido.pagar(new EstadoCriado());

        pedido.cancelar(new EstadoPago());

        assertEquals(StatusPedido.CANCELAMENTO_SOLICITADO, pedido.getStatus());
        assertEquals(8, produto.getEstoque());
    }

    @Test
    void deveEstornarPedidoEDevolverEstoque() {
        Usuario usuario = novoUsuario();
        Produto produto = novoProdutoComEstoque(10);
        Pedido pedido = new Pedido(usuario);

        pedido.adicionarItem(produto, 2);
        pedido.pagar(new EstadoCriado());
        pedido.cancelar(new EstadoPago());

        pedido.estornar(new EstadoCancelamentoSolicitado());

        assertEquals(StatusPedido.ESTORNADO, pedido.getStatus());
        assertEquals(10, produto.getEstoque());
    }

    private Usuario novoUsuario() {
        return new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );
    }

    private Produto novoProdutoComEstoque(Integer estoque) {
        return new Produto(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                estoque
        );
    }
}
