package br.com.api.pedidos.order.service;

import br.com.api.pedidos.coupon.service.CupomService;
import br.com.api.pedidos.order.dto.AdicionarPedidoRequestDTO;
import br.com.api.pedidos.order.dto.PedidoResponseDTO;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.event.PedidoCriadoEvent;
import br.com.api.pedidos.order.promotion.engine.MotorPromocao;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
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
                List.of(new DescontoQuantidade())
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

        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponseDTO response = pedidoService.criarPedido(usuario);

        assertEquals(StatusPedido.CRIADO, response.status());
        assertEquals(BigDecimal.ZERO, response.valorBruto());

        verify(pedidoRepository).save(any(Pedido.class));
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

        AdicionarPedidoRequestDTO requestDTO =
                new AdicionarPedidoRequestDTO(1L, 2);

        PedidoResponseDTO responseDTO =
                pedidoService.adicionarItemPedido(1L, requestDTO, usuario);

        assertEquals(1, responseDTO.itens().size());
        assertEquals(BigDecimal.valueOf(200), responseDTO.valorBruto());
        assertEquals(8, produto.getEstoque());

        verify(pedidoRepository).findByIdAndUsuario(1L, usuario);
        verify(produtoRepository).findById(1L);
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
