package br.com.api.pedidos.notification.service;

import br.com.api.pedidos.notification.dto.NotificacaoResponseDTO;
import br.com.api.pedidos.notification.entity.Notificacao;
import br.com.api.pedidos.notification.entity.TipoNotificacao;
import br.com.api.pedidos.notification.repository.NotificacaoRepository;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    private NotificacaoService notificacaoService;

    @BeforeEach
    void setUp() {
        notificacaoService = new NotificacaoService(
                notificacaoRepository,
                pedidoRepository
        );
    }

    @Test
    void deveMarcarNotificacaoComoLida() {
        Usuario usuario = new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );

        ReflectionTestUtils.setField(usuario, "id", 1L);
        Pedido pedido = new Pedido(usuario);
        ReflectionTestUtils.setField(pedido, "id", 10L);

        Notificacao notificacao = new Notificacao(
                usuario,
                pedido,
                "Pagamento confirmado",
                "O pagamento do pedido #10 foi confirmado",
                TipoNotificacao.PEDIDO_PAGO
        );

        ReflectionTestUtils.setField(notificacao, "id", 100L);

        when(notificacaoRepository.findByIdAndUsuario(100L, usuario))
                .thenReturn(Optional.of(notificacao));

        NotificacaoResponseDTO response =
                notificacaoService.marcarComoLida(100L, usuario);

        assertTrue(response.lida());
        verify(notificacaoRepository).findByIdAndUsuario(100L, usuario);
    }
}
