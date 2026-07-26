package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoPagoTest {

    private EstadoPago estadoPago;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        estadoPago = new EstadoPago();
        pedido = novoPedido();
    }

    @Nested
    class IdentificacaoEPermissoes {

        @Test
        void deveRepresentarStatusPago() {
            assertEquals(
                    StatusPedido.PAGO,
                    estadoPago.getStatus()
            );
        }

        @Test
        void naoDevePermitirAlterarItensNemAplicarCupom() {
            assertAll(
                    () -> assertFalse(
                            estadoPago.permiteAlterarItens()
                    ),
                    () -> assertFalse(
                            estadoPago.permiteAplicarCupom()
                    )
            );
        }
    }

    @Nested
    class OperacoesPermitidas {

        @Test
        void devePermitirEnviarPedido() {
            StatusPedido proximoStatus =
                    estadoPago.enviar(pedido);

            assertEquals(
                    StatusPedido.ENVIADO,
                    proximoStatus
            );
        }

        @Test
        void devePermitirSolicitarCancelamento() {
            StatusPedido proximoStatus =
                    estadoPago.cancelar(pedido);

            assertEquals(
                    StatusPedido.CANCELAMENTO_SOLICITADO,
                    proximoStatus
            );
        }
    }

    @Nested
    class OperacoesNaoPermitidas {

        @Test
        void naoDevePermitirPagarNovamente() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoPago.pagar(pedido)
            );

            assertEquals(
                    "Pedido com status PAGO não pode ser pago.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirAguardarPagamento() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoPago.aguardarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status PAGO não pode aguardar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirConfirmarPagamentoNovamente() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoPago.confirmarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status PAGO não pode confirmar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEntregarDiretamente() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoPago.entregar(pedido)
            );

            assertEquals(
                    "Pedido com status PAGO não pode ser entregue.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEstornarDiretamente() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoPago.estornar(pedido)
            );

            assertEquals(
                    "Pedido com status PAGO não pode ser estornado.",
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