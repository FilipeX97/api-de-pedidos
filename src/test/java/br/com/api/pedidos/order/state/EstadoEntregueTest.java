package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoEntregueTest {

    private EstadoEntregue estadoEntregue;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        estadoEntregue = new EstadoEntregue();
        pedido = novoPedido();
    }

    @Nested
    class IdentificacaoEPermissoes {

        @Test
        void deveRepresentarStatusEntregue() {
            assertEquals(
                    StatusPedido.ENTREGUE,
                    estadoEntregue.getStatus()
            );
        }

        @Test
        void naoDevePermitirAlterarItensNemAplicarCupom() {
            assertAll(
                    () -> assertFalse(
                            estadoEntregue.permiteAlterarItens()
                    ),
                    () -> assertFalse(
                            estadoEntregue.permiteAplicarCupom()
                    )
            );
        }
    }

    @Nested
    class OperacoesNaoPermitidas {

        @Test
        void naoDevePermitirPagar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEntregue.pagar(pedido)
            );

            assertEquals(
                    "Pedido com status ENTREGUE não pode ser pago.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirAguardarPagamento() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEntregue.aguardarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status ENTREGUE não pode aguardar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirConfirmarPagamento() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEntregue.confirmarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status ENTREGUE não pode confirmar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEnviar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEntregue.enviar(pedido)
            );

            assertEquals(
                    "Pedido com status ENTREGUE não pode ser enviado.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEntregarNovamente() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEntregue.entregar(pedido)
            );

            assertEquals(
                    "Pedido com status ENTREGUE não pode ser entregue.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirCancelar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEntregue.cancelar(pedido)
            );

            assertEquals(
                    "Pedido com status ENTREGUE não pode ser cancelado.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEstornar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEntregue.estornar(pedido)
            );

            assertEquals(
                    "Pedido com status ENTREGUE não pode ser estornado.",
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
}