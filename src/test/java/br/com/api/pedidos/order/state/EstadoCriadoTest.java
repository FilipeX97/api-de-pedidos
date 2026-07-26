package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EstadoCriadoTest {

    private EstadoCriado estadoCriado;

    @BeforeEach
    void setUp() {
        estadoCriado = new EstadoCriado();
    }

    @Nested
    class IdentificacaoEPermissoes {

        @Test
        void deveRepresentarStatusCriado() {
            assertEquals(
                    StatusPedido.CRIADO,
                    estadoCriado.getStatus()
            );
        }

        @Test
        void devePermitirAlterarItens() {
            assertTrue(
                    estadoCriado.permiteAlterarItens()
            );
        }

        @Test
        void devePermitirAplicarCupom() {
            assertTrue(
                    estadoCriado.permiteAplicarCupom()
            );
        }
    }

    @Nested
    class Pagamento {

        @Test
        void devePermitirPagarPedidoComItem() {
            Pedido pedido = novoPedidoComItem();

            StatusPedido proximoStatus =
                    estadoCriado.pagar(pedido);

            assertEquals(
                    StatusPedido.PAGO,
                    proximoStatus
            );
        }

        @Test
        void naoDevePermitirPagarPedidoVazio() {
            Pedido pedido = novoPedido();

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCriado.pagar(pedido)
            );

            assertEquals(
                    "Não é possível iniciar pagamento de um pedido sem itens.",
                    excecao.getMessage()
            );
        }

        @Test
        void devePermitirAguardarPagamentoQuandoPedidoTemItem() {
            Pedido pedido = novoPedidoComItem();

            StatusPedido proximoStatus =
                    estadoCriado.aguardarPagamento(pedido);

            assertEquals(
                    StatusPedido.AGUARDANDO_PAGAMENTO,
                    proximoStatus
            );
        }

        @Test
        void naoDevePermitirAguardarPagamentoDePedidoVazio() {
            Pedido pedido = novoPedido();

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCriado.aguardarPagamento(pedido)
            );

            assertEquals(
                    "Não é possível iniciar pagamento de um pedido sem itens.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirConfirmarPagamentoDiretamente() {
            Pedido pedido = novoPedidoComItem();

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCriado.confirmarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status CRIADO não pode confirmar pagamento.",
                    excecao.getMessage()
            );
        }
    }

    @Nested
    class Cancelamento {

        @Test
        void devePermitirCancelarPedido() {
            Pedido pedido = novoPedido();

            StatusPedido proximoStatus =
                    estadoCriado.cancelar(pedido);

            assertEquals(
                    StatusPedido.CANCELADO,
                    proximoStatus
            );
        }
    }

    @Nested
    class OperacoesNaoPermitidas {

        @Test
        void naoDevePermitirEnviarPedido() {
            Pedido pedido = novoPedidoComItem();

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCriado.enviar(pedido)
            );

            assertEquals(
                    "Pedido com status CRIADO não pode ser enviado.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEntregarPedido() {
            Pedido pedido = novoPedidoComItem();

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCriado.entregar(pedido)
            );

            assertEquals(
                    "Pedido com status CRIADO não pode ser entregue.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEstornarPedido() {
            Pedido pedido = novoPedidoComItem();

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCriado.estornar(pedido)
            );

            assertEquals(
                    "Pedido com status CRIADO não pode ser estornado.",
                    excecao.getMessage()
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

    private Pedido novoPedidoComItem() {
        Pedido pedido = novoPedido();

        Produto produto = new Produto(
                "Mouse",
                "Mouse sem fio",
                new BigDecimal("100.00"),
                10
        );

        pedido.adicionarItem(
                produto,
                1
        );

        return pedido;
    }
}