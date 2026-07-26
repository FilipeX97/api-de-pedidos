package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoEnviadoTest {

    private EstadoEnviado estadoEnviado;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        estadoEnviado = new EstadoEnviado();
        pedido = novoPedido();
    }

    @Nested
    class IdentificacaoEPermissoes {

        @Test
        void deveRepresentarStatusEnviado() {
            assertEquals(
                    StatusPedido.ENVIADO,
                    estadoEnviado.getStatus()
            );
        }

        @Test
        void naoDevePermitirAlterarItensNemAplicarCupom() {
            assertAll(
                    () -> assertFalse(
                            estadoEnviado.permiteAlterarItens()
                    ),
                    () -> assertFalse(
                            estadoEnviado.permiteAplicarCupom()
                    )
            );
        }
    }

    @Nested
    class OperacoesPermitidas {

        @Test
        void devePermitirEntregarPedido() {
            StatusPedido proximoStatus =
                    estadoEnviado.entregar(pedido);

            assertEquals(
                    StatusPedido.ENTREGUE,
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
                    () -> estadoEnviado.pagar(pedido)
            );

            assertEquals(
                    "Pedido com status ENVIADO não pode ser pago.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirAguardarPagamento() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEnviado.aguardarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status ENVIADO não pode aguardar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirConfirmarPagamento() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEnviado.confirmarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status ENVIADO não pode confirmar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEnviarNovamente() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEnviado.enviar(pedido)
            );

            assertEquals(
                    "Pedido com status ENVIADO não pode ser enviado.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirCancelarDiretamente() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEnviado.cancelar(pedido)
            );

            assertEquals(
                    "Pedido com status ENVIADO não pode ser cancelado.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEstornar() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoEnviado.estornar(pedido)
            );

            assertEquals(
                    "Pedido com status ENVIADO não pode ser estornado.",
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