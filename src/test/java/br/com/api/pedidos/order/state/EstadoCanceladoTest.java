package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoCanceladoTest {

    private EstadoCancelado estadoCancelado;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        estadoCancelado = new EstadoCancelado();
        pedido = novoPedido();
    }

    @Nested
    class IdentificacaoEPermissoes {

        @Test
        void deveRepresentarStatusCancelado() {
            assertEquals(
                    StatusPedido.CANCELADO,
                    estadoCancelado.getStatus()
            );
        }

        @Test
        void naoDevePermitirAlterarItensNemAplicarCupom() {
            assertAll(
                    () -> assertFalse(
                            estadoCancelado.permiteAlterarItens()
                    ),
                    () -> assertFalse(
                            estadoCancelado.permiteAplicarCupom()
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
                    () -> estadoCancelado.pagar(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELADO não pode ser pago.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirAguardarPagamento() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCancelado.aguardarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELADO não pode aguardar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirConfirmarPagamento() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCancelado.confirmarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELADO não pode confirmar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEnviar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCancelado.enviar(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELADO não pode ser enviado.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEntregar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCancelado.entregar(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELADO não pode ser entregue.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirCancelarNovamente() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCancelado.cancelar(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELADO não pode ser cancelado.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEstornar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCancelado.estornar(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELADO não pode ser estornado.",
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