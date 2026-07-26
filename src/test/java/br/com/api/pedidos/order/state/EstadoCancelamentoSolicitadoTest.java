package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoCancelamentoSolicitadoTest {

    private EstadoCancelamentoSolicitado estadoCancelamentoSolicitado;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        estadoCancelamentoSolicitado =
                new EstadoCancelamentoSolicitado();

        pedido = novoPedido();
    }

    @Nested
    class IdentificacaoEPermissoes {

        @Test
        void deveRepresentarStatusCancelamentoSolicitado() {
            assertEquals(
                    StatusPedido.CANCELAMENTO_SOLICITADO,
                    estadoCancelamentoSolicitado.getStatus()
            );
        }

        @Test
        void naoDevePermitirAlterarItensNemAplicarCupom() {
            assertAll(
                    () -> assertFalse(
                            estadoCancelamentoSolicitado
                                    .permiteAlterarItens()
                    ),
                    () -> assertFalse(
                            estadoCancelamentoSolicitado
                                    .permiteAplicarCupom()
                    )
            );
        }
    }

    @Nested
    class OperacoesPermitidas {

        @Test
        void devePermitirEstornarPedido() {
            StatusPedido proximoStatus =
                    estadoCancelamentoSolicitado
                            .estornar(pedido);

            assertEquals(
                    StatusPedido.ESTORNADO,
                    proximoStatus
            );
        }
    }

    @Nested
    class OperacoesNaoPermitidas {

        @Test
        void naoDevePermitirPagar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCancelamentoSolicitado
                            .pagar(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELAMENTO_SOLICITADO não pode ser pago.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirAguardarPagamento() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCancelamentoSolicitado
                            .aguardarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELAMENTO_SOLICITADO não pode aguardar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirConfirmarPagamento() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCancelamentoSolicitado
                            .confirmarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELAMENTO_SOLICITADO não pode confirmar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEnviar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCancelamentoSolicitado
                            .enviar(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELAMENTO_SOLICITADO não pode ser enviado.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEntregar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCancelamentoSolicitado
                            .entregar(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELAMENTO_SOLICITADO não pode ser entregue.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirCancelarNovamente() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoCancelamentoSolicitado
                            .cancelar(pedido)
            );

            assertEquals(
                    "Pedido com status CANCELAMENTO_SOLICITADO não pode ser cancelado.",
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