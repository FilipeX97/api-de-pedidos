package br.com.api.pedidos.order.listener;

import br.com.api.pedidos.order.event.PedidoPagoEvent;
import br.com.api.pedidos.order.history.service.HistoricoPedidoService;
import br.com.api.pedidos.order.state.StatusPedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HistoricoPedidoListenerTest {

    @Mock
    private HistoricoPedidoService historicoPedidoService;

    @InjectMocks
    private HistoricoPedidoListener historicoPedidoListener;

    @Test
    void deveCriarHistoricoQuandoPedidoForPago() {
        PedidoPagoEvent event = new PedidoPagoEvent(
                10L,
                1L,
                BigDecimal.valueOf(100),
                LocalDateTime.now()
        );

        historicoPedidoListener.aoPagarPedido(event);

        verify(historicoPedidoService).registrar(
                10L,
                StatusPedido.PAGO,
                "Pedido pago com sucesso"
        );
    }
}
