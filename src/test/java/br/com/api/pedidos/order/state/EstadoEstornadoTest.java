package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoEstornadoTest {

    private EstadoEstornado estadoEstornado;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        estadoEstornado = new EstadoEstornado();
        pedido = novoPedido();
    }

    @Nested
    class IdentificacaoEPermissoes {

        @Test
        void deveRepresentarStatusEstornado() {
            assertEquals(
                    StatusPedido.ESTORNADO,
                    estadoEstornado.getStatus()
            );
        }

        @Test
        void naoDevePermitirAlterarItensNemAplicarCupom() {
            assertAll(
                    () -> assertFalse(
                            estadoEstornado.permiteAlterarItens()
                    ),
                    () -> assertFalse(
                            estadoEstornado.permiteAplicarCupom()
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
                    () -> estadoEstornado.pagar(pedido)
            );

            assertEquals(
                    "Pedido com status ESTORNADO não pode ser pago.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirAguardarPagamento() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEstornado.aguardarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status ESTORNADO não pode aguardar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirConfirmarPagamento() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEstornado.confirmarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status ESTORNADO não pode confirmar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEnviar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEstornado.enviar(pedido)
            );

            assertEquals(
                    "Pedido com status ESTORNADO não pode ser enviado.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEntregar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEstornado.entregar(pedido)
            );

            assertEquals(
                    "Pedido com status ESTORNADO não pode ser entregue.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirCancelar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEstornado.cancelar(pedido)
            );

            assertEquals(
                    "Pedido com status ESTORNADO não pode ser cancelado.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEstornarNovamente() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEstornado.estornar(pedido)
            );

            assertEquals(
                    "Pedido com status ESTORNADO não pode ser estornado.",
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