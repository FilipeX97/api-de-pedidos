package br.com.api.pedidos.order.state;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoAguardandoPagamentoTest {

    private EstadoAguardandoPagamento estadoAguardandoPagamento;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        estadoAguardandoPagamento =
                new EstadoAguardandoPagamento();

        pedido = novoPedido();
    }

    @Nested
    class IdentificacaoEPermissoes {

        @Test
        void deveRepresentarStatusAguardandoPagamento() {
            assertEquals(
                    StatusPedido.AGUARDANDO_PAGAMENTO,
                    estadoAguardandoPagamento.getStatus()
            );
        }

        @Test
        void naoDevePermitirAlterarItensNemAplicarCupom() {
            assertAll(
                    () -> assertFalse(
                            estadoAguardandoPagamento
                                    .permiteAlterarItens()
                    ),
                    () -> assertFalse(
                            estadoAguardandoPagamento
                                    .permiteAplicarCupom()
                    )
            );
        }
    }

    @Nested
    class OperacoesPermitidas {

        @Test
        void devePermitirConfirmarPagamento() {
            StatusPedido proximoStatus =
                    estadoAguardandoPagamento
                            .confirmarPagamento(pedido);

            assertEquals(
                    StatusPedido.PAGO,
                    proximoStatus
            );
        }

        @Test
        void devePermitirCancelarPedido() {
            StatusPedido proximoStatus =
                    estadoAguardandoPagamento
                            .cancelar(pedido);

            assertEquals(
                    StatusPedido.CANCELADO,
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
                    () -> estadoAguardandoPagamento
                            .pagar(pedido)
            );

            assertEquals(
                    "Pedido com status AGUARDANDO_PAGAMENTO não pode ser pago.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirAguardarPagamentoNovamente() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoAguardandoPagamento
                            .aguardarPagamento(pedido)
            );

            assertEquals(
                    "Pedido com status AGUARDANDO_PAGAMENTO não pode aguardar pagamento.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEnviarPedido() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoAguardandoPagamento
                            .enviar(pedido)
            );

            assertEquals(
                    "Pedido com status AGUARDANDO_PAGAMENTO não pode ser enviado.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEntregarPedido() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoAguardandoPagamento
                            .entregar(pedido)
            );

            assertEquals(
                    "Pedido com status AGUARDANDO_PAGAMENTO não pode ser entregue.",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDevePermitirEstornarPedido() {
            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> estadoAguardandoPagamento
                            .estornar(pedido)
            );

            assertEquals(
                    "Pedido com status AGUARDANDO_PAGAMENTO não pode ser estornado.",
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