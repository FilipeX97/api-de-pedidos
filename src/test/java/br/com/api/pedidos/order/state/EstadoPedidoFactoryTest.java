package br.com.api.pedidos.order.state;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EstadoPedidoFactoryTest {

    @Test
    void deveObterEstadoCorrespondenteParaTodosOsStatus() {
        EstadoCriado estadoCriado =
                new EstadoCriado();

        EstadoAguardandoPagamento estadoAguardandoPagamento =
                new EstadoAguardandoPagamento();

        EstadoPago estadoPago =
                new EstadoPago();

        EstadoEnviado estadoEnviado =
                new EstadoEnviado();

        EstadoEntregue estadoEntregue =
                new EstadoEntregue();

        EstadoCancelamentoSolicitado estadoCancelamentoSolicitado =
                new EstadoCancelamentoSolicitado();

        EstadoCancelado estadoCancelado =
                new EstadoCancelado();

        EstadoEstornado estadoEstornado =
                new EstadoEstornado();

        EstadoPedidoFactory factory =
                new EstadoPedidoFactory(
                        List.of(
                                estadoCriado,
                                estadoAguardandoPagamento,
                                estadoPago,
                                estadoEnviado,
                                estadoEntregue,
                                estadoCancelamentoSolicitado,
                                estadoCancelado,
                                estadoEstornado
                        )
                );

        assertAll(
                () -> assertSame(
                        estadoCriado,
                        factory.obter(StatusPedido.CRIADO)
                ),
                () -> assertSame(
                        estadoAguardandoPagamento,
                        factory.obter(
                                StatusPedido.AGUARDANDO_PAGAMENTO
                        )
                ),
                () -> assertSame(
                        estadoPago,
                        factory.obter(StatusPedido.PAGO)
                ),
                () -> assertSame(
                        estadoEnviado,
                        factory.obter(StatusPedido.ENVIADO)
                ),
                () -> assertSame(
                        estadoEntregue,
                        factory.obter(StatusPedido.ENTREGUE)
                ),
                () -> assertSame(
                        estadoCancelamentoSolicitado,
                        factory.obter(
                                StatusPedido.CANCELAMENTO_SOLICITADO
                        )
                ),
                () -> assertSame(
                        estadoCancelado,
                        factory.obter(StatusPedido.CANCELADO)
                ),
                () -> assertSame(
                        estadoEstornado,
                        factory.obter(StatusPedido.ESTORNADO)
                )
        );
    }

    @Test
    void naoDeveObterEstadoQuandoStatusForNulo() {
        EstadoPedidoFactory factory =
                novaFactoryCompleta();

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> factory.obter(null)
        );

        assertEquals(
                "Status do pedido não informado",
                excecao.getMessage()
        );
    }

    @Test
    void naoDeveObterStatusSemEstadoRegistrado() {
        EstadoPedidoFactory factory =
                new EstadoPedidoFactory(
                        List.of(
                                new EstadoCriado()
                        )
                );

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> factory.obter(StatusPedido.PAGO)
        );

        assertEquals(
                "Status do pedido não suportado: PAGO",
                excecao.getMessage()
        );
    }

    @Test
    void naoDeveRegistrarDoisEstadosParaMesmoStatus() {
        EstadoCriado primeiroEstadoCriado =
                new EstadoCriado();

        EstadoCriado segundoEstadoCriado =
                new EstadoCriado();

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> new EstadoPedidoFactory(
                        List.of(
                                primeiroEstadoCriado,
                                segundoEstadoCriado
                        )
                )
        );

        assertEquals(
                "Já existe um estado para o status CRIADO",
                excecao.getMessage()
        );
    }

    private EstadoPedidoFactory novaFactoryCompleta() {
        return new EstadoPedidoFactory(
                List.of(
                        new EstadoCriado(),
                        new EstadoAguardandoPagamento(),
                        new EstadoPago(),
                        new EstadoEnviado(),
                        new EstadoEntregue(),
                        new EstadoCancelamentoSolicitado(),
                        new EstadoCancelado(),
                        new EstadoEstornado()
                )
        );
    }
}