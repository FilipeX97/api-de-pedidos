package br.com.api.pedidos.order.listener;

import br.com.api.pedidos.notification.entity.TipoNotificacao;
import br.com.api.pedidos.notification.service.NotificacaoService;
import br.com.api.pedidos.order.event.PedidoPagoEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificacaoPedidoListenerTest {

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private NotificacaoPedidoListener notificacaoPedidoListener;

    @Test
    void deveCriarNotificacaoQuandoPedidoForPago() {
        PedidoPagoEvent event = new PedidoPagoEvent(
                10L,
                1L,
                BigDecimal.valueOf(100),
                LocalDateTime.now()
        );

        notificacaoPedidoListener.aoPagarPedido(event);

        verify(notificacaoService).criar(
                10L,
                "Pagamento confirmado",
                "O pagamento do pedido #10 foi confirmado",
                TipoNotificacao.PEDIDO_PAGO
        );
    }
}
