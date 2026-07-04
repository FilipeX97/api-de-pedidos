package br.com.api.pedidos.order.service;

import br.com.api.pedidos.coupon.entity.Cupom;
import br.com.api.pedidos.coupon.service.CupomService;
import br.com.api.pedidos.order.dto.AdicionarPedidoRequestDTO;
import br.com.api.pedidos.order.dto.PedidoResponseDTO;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.event.*;
import br.com.api.pedidos.order.promotion.engine.MotorPromocao;
import br.com.api.pedidos.order.promotion.strategy.DescontoCupom;
import br.com.api.pedidos.order.promotion.strategy.DescontoQuantidade;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.order.state.*;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.product.repository.ProdutoRepository;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CupomService cupomService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        MotorPromocao motorPromocao = new MotorPromocao(
                List.of(
                        new DescontoQuantidade(),
                        new DescontoCupom()
                )
        );

        EstadoPedidoFactory estadoPedidoFactory = new EstadoPedidoFactory(
                List.of(
                        new EstadoCriado(),
                        new EstadoPago(),
                        new EstadoEnviado(),
                        new EstadoEntregue(),
                        new EstadoCancelado(),
                        new EstadoCancelamentoSolicitado(),
                        new EstadoEstornado()
                )
        );

        pedidoService = new PedidoService(
                pedidoRepository,
                produtoRepository,
                cupomService,
                motorPromocao,
                estadoPedidoFactory,
                eventPublisher
        );
    }

    @Test
    void deveCriarPedido() {
        Usuario usuario = novoUsuario();

        when(pedidoRepository.saveAndFlush(any(Pedido.class)))
                .thenAnswer(invocation -> {
                    Pedido pedido = invocation.getArgument(0);
                    definirId(pedido, 1L);
                    return pedido;
                });

        PedidoResponseDTO response = pedidoService.criarPedido(usuario);

        assertEquals(StatusPedido.CRIADO, response.status());
        assertEquals(BigDecimal.ZERO, response.valorBruto());

        verify(pedidoRepository).saveAndFlush(any(Pedido.class));
        verify(eventPublisher).publishEvent(any(PedidoCriadoEvent.class));
    }

    @Test
    void deveAdicionarItemAoPedido() {
        Usuario usuario = novoUsuario();
        Pedido pedido = new Pedido(usuario);
        Produto produto = novoProdutoComEstoque(10);

        when(pedidoRepository.findByIdAndUsuario(1L, usuario))
                .thenReturn(Optional.of(pedido));

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(pedidoRepository.saveAndFlush(any(Pedido.class)))
                .thenAnswer(invocation -> {
                    Pedido pedidoSalvo = invocation.getArgument(0);

                    definirId(pedidoSalvo, 1L);

                    pedidoSalvo.getItens().forEach(item -> {
                        if (item.getId() == null) {
                            definirId(item, 1L);
                        }
                    });

                    return pedidoSalvo;
                });

        AdicionarPedidoRequestDTO requestDTO =
                new AdicionarPedidoRequestDTO(1L, 2);

        PedidoResponseDTO responseDTO =
                pedidoService.adicionarItemPedido(1L, requestDTO, usuario);

        assertEquals(1, responseDTO.itens().size());
        assertEquals(1L, responseDTO.itens().getFirst().itemPedidoId());
        assertEquals(BigDecimal.valueOf(200), responseDTO.valorBruto());
        assertEquals(8, produto.getEstoque());

        verify(pedidoRepository).findByIdAndUsuario(1L, usuario);
        verify(produtoRepository).findById(1L);
        verify(pedidoRepository).saveAndFlush(any(Pedido.class));
    }

    @Test
    void naoDeveAdicionarItemQuandoPedidoNaoExiste() {
        Usuario usuario = novoUsuario();

        when(pedidoRepository.findByIdAndUsuario(1L, usuario))
                .thenReturn(Optional.empty());

        AdicionarPedidoRequestDTO dto =
                new AdicionarPedidoRequestDTO(1L, 2);

        assertThrows(
                RuntimeException.class,
                () -> pedidoService.adicionarItemPedido(1L, dto, usuario)
        );

        verify(produtoRepository, never()).findById(anyLong());
    }

    @Test
    void devePublicarEventoAoPagarPedido() {
        Usuario usuario = novoUsuarioComId(1L);
        Pedido pedido = novoPedidoComItem(usuario);
        definirId(pedido, 10L);

        when(pedidoRepository.findByIdAndUsuario(10L, usuario))
                .thenReturn(Optional.of(pedido));

        PedidoResponseDTO response = pedidoService.pagarPedido(10L, usuario);
        assertEquals(StatusPedido.PAGO, response.status());

        ArgumentCaptor<PedidoPagoEvent> captor =
                ArgumentCaptor.forClass(PedidoPagoEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());
        PedidoPagoEvent event = captor.getValue();

        assertEquals(10L, event.idPedido());
        assertEquals(1L, event.idUsuario());
        assertEquals(BigDecimal.valueOf(100), event.valorFinal());
    }

    @Test
    void devePublicarEventoAoAplicarCupom() {
        Usuario usuario = novoUsuarioComId(1L);
        Pedido pedido = novoPedidoComItem(usuario);
        definirId(pedido, 10L);
        Cupom cupom = novoCupomValido();
        definirId(cupom, 5L);

        when(pedidoRepository.findByIdAndUsuario(10L, usuario))
                .thenReturn(Optional.of(pedido));

        when(cupomService.buscarCupomValido("DESC10"))
                .thenReturn(cupom);

        PedidoResponseDTO response = pedidoService.aplicarCupom(
                10L,
                usuario,
                "DESC10"
        );

        assertEquals("DESC10", response.codigoCupom());

        ArgumentCaptor<CupomAplicadoEvent> captor =
                ArgumentCaptor.forClass(CupomAplicadoEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        CupomAplicadoEvent event = captor.getValue();

        assertEquals(10L, event.idPedido());
        assertEquals(1L, event.idUsuario());
        assertEquals("DESC10", event.codigoCupom());
        assertEquals(StatusPedido.CRIADO, event.statusPedido());
    }

    @Test
    void devePublicarEventoAoCancelarPedido() {
        Usuario usuario = novoUsuarioComId(1L);
        Pedido pedido = new Pedido(usuario);
        definirId(pedido, 10L);

        when(pedidoRepository.findByIdAndUsuario(10L, usuario))
                .thenReturn(Optional.of(pedido));

        PedidoResponseDTO response = pedidoService.cancelarPedido(10L, usuario);

        assertEquals(StatusPedido.CANCELADO, response.status());

        ArgumentCaptor<PedidoCanceladoEvent> captor =
                ArgumentCaptor.forClass(PedidoCanceladoEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        PedidoCanceladoEvent event = captor.getValue();

        assertEquals(10L, event.idPedido());
        assertEquals(1L, event.idUsuario());
        assertEquals(StatusPedido.CRIADO, event.statusAnterior());
        assertEquals(StatusPedido.CANCELADO, event.statusNovo());
    }

    @Test
    void devePublicarEventoAoEstornarPedido() {
        Usuario usuario = novoUsuarioComId(1L);
        Pedido pedido = novoPedidoComItem(usuario);
        definirId(pedido, 10L);
        pedido.pagar(new EstadoCriado());
        pedido.cancelar(new EstadoPago());

        when(pedidoRepository.findById(10L))
                .thenReturn(Optional.of(pedido));

        PedidoResponseDTO response = pedidoService.estornarPedido(10L);

        assertEquals(StatusPedido.ESTORNADO, response.status());

        ArgumentCaptor<PedidoEstornadoEvent> captor =
                ArgumentCaptor.forClass(PedidoEstornadoEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        PedidoEstornadoEvent event = captor.getValue();

        assertEquals(10L, event.idPedido());
        assertEquals(1L, event.idUsuario());
        assertEquals(BigDecimal.valueOf(100), event.valorFinal());
    }

    private Usuario novoUsuarioComId(Long id) {
        Usuario usuario = novoUsuario();
        definirId(usuario, id);
        return usuario;
    }

    private Pedido novoPedidoComItem(Usuario usuario) {
        Pedido pedido = new Pedido(usuario);
        Produto produto = novoProdutoComEstoque(10);
        definirId(produto, 1L);
        pedido.adicionarItem(produto, 1);
        return pedido;
    }

    private Cupom novoCupomValido() {
        return new Cupom(
                "DESC10",
                BigDecimal.valueOf(0.10),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                100
        );
    }

    private void definirId(Object objeto, Long id) {
        ReflectionTestUtils.setField(objeto, "id", id);
    }

    private Usuario novoUsuario() {
        return new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );
    }

    private Produto novoProdutoComEstoque(Integer estoque) {
        return new Produto(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                estoque
        );
    }
}
