package br.com.api.pedidos.order.service;

import br.com.api.pedidos.coupon.entity.Cupom;
import br.com.api.pedidos.coupon.service.CupomService;
import br.com.api.pedidos.order.dto.AdicionarPedidoRequestDTO;
import br.com.api.pedidos.order.dto.AlterarQuantidadeItemRequestDTO;
import br.com.api.pedidos.order.dto.PedidoResponseDTO;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.event.*;
import br.com.api.pedidos.order.promotion.engine.MotorPromocao;
import br.com.api.pedidos.order.promotion.strategy.DescontoClienteVip;
import br.com.api.pedidos.order.promotion.strategy.DescontoCupom;
import br.com.api.pedidos.order.promotion.strategy.DescontoQuantidade;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.order.state.*;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.product.repository.ProdutoRepository;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    private static final Long ID_USUARIO = 1L;
    private static final Long ID_PEDIDO = 10L;
    private static final Long ID_PRODUTO = 20L;
    private static final Long ID_ITEM = 30L;

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
                        new DescontoClienteVip(),
                        new DescontoQuantidade(),
                        new DescontoCupom()
                )
        );

        EstadoPedidoFactory estadoPedidoFactory = new EstadoPedidoFactory(
                List.of(
                        new EstadoCriado(),
                        new EstadoAguardandoPagamento(),
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

    @Nested
    class CriacaoEConsulta {

        @Test
        void deveCriarPedidoEPublicarEvento() {
            Usuario usuario = novoUsuario();
            configurarSaveAndFlush();

            PedidoResponseDTO resposta =
                    pedidoService.criarPedido(usuario);

            assertAll(
                    () -> assertEquals(
                            ID_PEDIDO,
                            resposta.idPedido()
                    ),
                    () -> assertEquals(
                            ID_USUARIO,
                            resposta.idUsuario()
                    ),
                    () -> assertEquals(
                            StatusPedido.CRIADO,
                            resposta.status()
                    ),
                    () -> assertBigDecimal(
                            "0.00",
                            resposta.valorFinal()
                    ),
                    () -> assertTrue(
                            resposta.itens().isEmpty()
                    )
            );

            PedidoCriadoEvent evento =
                    capturarEvento(PedidoCriadoEvent.class);

            assertAll(
                    () -> assertEquals(
                            ID_PEDIDO,
                            evento.idPedido()
                    ),
                    () -> assertEquals(
                            ID_USUARIO,
                            evento.idUsuario()
                    ),
                    () -> assertNotNull(
                            evento.dataHora()
                    )
            );
        }

        @Test
        void naoDeveCriarPedidoSemUsuario() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pedidoService.criarPedido(null)
            );

            assertEquals(
                    "Usuário é obrigatório",
                    excecao.getMessage()
            );

            verifyNoInteractions(
                    pedidoRepository,
                    eventPublisher
            );
        }

        @Test
        void deveBuscarPedidoDoUsuario() {
            Usuario usuario = novoUsuario();

            Pedido pedido = novoPedidoComItem(
                    usuario,
                    2,
                    20
            ).pedido();

            mockPedidoDoUsuario(
                    usuario,
                    pedido
            );

            PedidoResponseDTO resposta =
                    pedidoService.buscarPedidoPorId(
                            ID_PEDIDO,
                            usuario
                    );

            assertAll(
                    () -> assertEquals(
                            ID_PEDIDO,
                            resposta.idPedido()
                    ),
                    () -> assertEquals(
                            StatusPedido.CRIADO,
                            resposta.status()
                    ),
                    () -> assertBigDecimal(
                            "200.00",
                            resposta.valorBruto()
                    ),
                    () -> assertEquals(
                            1,
                            resposta.itens().size()
                    )
            );
        }

        @Test
        void naoDeveBuscarPedidoInexistenteOuDeOutroUsuario() {
            Usuario usuario = novoUsuario();

            when(
                    pedidoRepository.findByIdAndUsuario(
                            ID_PEDIDO,
                            usuario
                    )
            ).thenReturn(Optional.empty());

            RuntimeException excecao = assertThrows(
                    RuntimeException.class,
                    () -> pedidoService.buscarPedidoPorId(
                            ID_PEDIDO,
                            usuario
                    )
            );

            assertEquals(
                    "Pedido não encontrado",
                    excecao.getMessage()
            );
        }
    }

    @Nested
    class Itens {

        @Test
        void deveAdicionarItemRecalcularValoresEReduzirEstoque() {
            Usuario usuario = novoUsuario();
            Pedido pedido = novoPedidoVazio(usuario);
            Produto produto = novoProduto(10);

            mockPedidoDoUsuario(
                    usuario,
                    pedido
            );

            when(
                    produtoRepository.findById(
                            ID_PRODUTO
                    )
            ).thenReturn(Optional.of(produto));

            configurarSaveAndFlush();

            PedidoResponseDTO resposta =
                    pedidoService.adicionarItemPedido(
                            ID_PEDIDO,
                            new AdicionarPedidoRequestDTO(
                                    ID_PRODUTO,
                                    2
                            ),
                            usuario
                    );

            assertAll(
                    () -> assertEquals(
                            1,
                            resposta.itens().size()
                    ),
                    () -> assertEquals(
                            ID_ITEM,
                            resposta.itens()
                                    .getFirst()
                                    .itemPedidoId()
                    ),
                    () -> assertEquals(
                            2,
                            resposta.itens()
                                    .getFirst()
                                    .quantidade()
                    ),
                    () -> assertBigDecimal(
                            "200.00",
                            resposta.valorFinal()
                    ),
                    () -> assertEquals(
                            8,
                            produto.getEstoque()
                    )
            );

            verify(pedidoRepository).saveAndFlush(pedido);
        }

        @Test
        void naoDeveAdicionarItemQuandoPedidoNaoExiste() {
            Usuario usuario = novoUsuario();

            when(
                    pedidoRepository.findByIdAndUsuario(
                            ID_PEDIDO,
                            usuario
                    )
            ).thenReturn(Optional.empty());

            RuntimeException excecao = assertThrows(
                    RuntimeException.class,
                    () -> pedidoService.adicionarItemPedido(
                            ID_PEDIDO,
                            new AdicionarPedidoRequestDTO(
                                    ID_PRODUTO,
                                    2
                            ),
                            usuario
                    )
            );

            assertEquals(
                    "Pedido não encontrado",
                    excecao.getMessage()
            );

            verify(
                    produtoRepository,
                    never()
            ).findById(anyLong());
        }

        @Test
        void naoDeveAdicionarProdutoInexistente() {
            Usuario usuario = novoUsuario();

            mockPedidoDoUsuario(
                    usuario,
                    novoPedidoVazio(usuario)
            );

            when(
                    produtoRepository.findById(
                            ID_PRODUTO
                    )
            ).thenReturn(Optional.empty());

            RuntimeException excecao = assertThrows(
                    RuntimeException.class,
                    () -> pedidoService.adicionarItemPedido(
                            ID_PEDIDO,
                            new AdicionarPedidoRequestDTO(
                                    ID_PRODUTO,
                                    2
                            ),
                            usuario
                    )
            );

            assertEquals(
                    "Produto não encontrado",
                    excecao.getMessage()
            );

            verify(
                    pedidoRepository,
                    never()
            ).saveAndFlush(any());
        }

        @Test
        void naoDeveAlterarItensQuandoPedidoEstaPago() {
            Usuario usuario = novoUsuario();
            Pedido pedido = novoPedidoPago(usuario);

            mockPedidoDoUsuario(
                    usuario,
                    pedido
            );

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pedidoService.adicionarItemPedido(
                            ID_PEDIDO,
                            new AdicionarPedidoRequestDTO(
                                    ID_PRODUTO,
                                    1
                            ),
                            usuario
                    )
            );

            assertEquals(
                    "Pedido com status PAGO não permite alterar itens.",
                    excecao.getMessage()
            );

            verifyNoInteractions(produtoRepository);
        }

        @Test
        void deveAlterarQuantidadeEAplicarDescontoPorQuantidade() {
            Usuario usuario = novoUsuario();

            PedidoComProduto fixture = novoPedidoComItem(
                    usuario,
                    2,
                    20
            );

            mockPedidoDoUsuario(
                    usuario,
                    fixture.pedido()
            );

            PedidoResponseDTO resposta =
                    pedidoService.alterarQuantidadeItemPedido(
                            ID_PEDIDO,
                            ID_ITEM,
                            new AlterarQuantidadeItemRequestDTO(10),
                            usuario
                    );

            assertAll(
                    () -> assertEquals(
                            10,
                            resposta.itens()
                                    .getFirst()
                                    .quantidade()
                    ),
                    () -> assertBigDecimal(
                            "1000.00",
                            resposta.valorBruto()
                    ),
                    () -> assertBigDecimal(
                            "100.00",
                            resposta.valorDesconto()
                    ),
                    () -> assertBigDecimal(
                            "900.00",
                            resposta.valorFinal()
                    ),
                    () -> assertEquals(
                            10,
                            fixture.produto().getEstoque()
                    )
            );
        }

        @Test
        void deveRemoverItemDevolverEstoqueERecalcularValores() {
            Usuario usuario = novoUsuario();

            PedidoComProduto fixture = novoPedidoComItem(
                    usuario,
                    2,
                    20
            );

            mockPedidoDoUsuario(
                    usuario,
                    fixture.pedido()
            );

            PedidoResponseDTO resposta =
                    pedidoService.removerItemPedido(
                            ID_PEDIDO,
                            ID_ITEM,
                            usuario
                    );

            assertAll(
                    () -> assertTrue(
                            resposta.itens().isEmpty()
                    ),
                    () -> assertBigDecimal(
                            "0.00",
                            resposta.valorFinal()
                    ),
                    () -> assertEquals(
                            20,
                            fixture.produto().getEstoque()
                    )
            );
        }
    }

    @Nested
    class CupomEPagamento {

        @Test
        void deveAplicarCupomRecalcularValoresEPublicarEvento() {
            Usuario usuario = novoUsuario();

            Pedido pedido = novoPedidoComItem(
                    usuario,
                    2,
                    20
            ).pedido();

            Cupom cupom = novoCupom(
                    "DESC10",
                    "0.10"
            );

            definirId(
                    cupom,
                    40L
            );

            mockPedidoDoUsuario(
                    usuario,
                    pedido
            );

            when(
                    cupomService.buscarCupomValido(
                            "DESC10"
                    )
            ).thenReturn(cupom);

            PedidoResponseDTO resposta =
                    pedidoService.aplicarCupom(
                            ID_PEDIDO,
                            usuario,
                            "DESC10"
                    );

            assertAll(
                    () -> assertEquals(
                            "DESC10",
                            resposta.codigoCupom()
                    ),
                    () -> assertBigDecimal(
                            "20.00",
                            resposta.valorDesconto()
                    ),
                    () -> assertBigDecimal(
                            "180.00",
                            resposta.valorFinal()
                    )
            );

            CupomAplicadoEvent evento =
                    capturarEvento(CupomAplicadoEvent.class);

            assertAll(
                    () -> assertEquals(
                            ID_PEDIDO,
                            evento.idPedido()
                    ),
                    () -> assertEquals(
                            ID_USUARIO,
                            evento.idUsuario()
                    ),
                    () -> assertEquals(
                            "DESC10",
                            evento.codigoCupom()
                    ),
                    () -> assertEquals(
                            StatusPedido.CRIADO,
                            evento.statusPedido()
                    )
            );
        }

        @Test
        void naoDeveAplicarCupomQuandoPedidoEstaPago() {
            Usuario usuario = novoUsuario();
            Pedido pedido = novoPedidoPago(usuario);

            mockPedidoDoUsuario(
                    usuario,
                    pedido
            );

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pedidoService.aplicarCupom(
                            ID_PEDIDO,
                            usuario,
                            "DESC10"
                    )
            );

            assertEquals(
                    "Pedido com status PAGO não permite aplicar cupom.",
                    excecao.getMessage()
            );

            verifyNoInteractions(
                    cupomService,
                    eventPublisher
            );
        }

        @Test
        void devePagarPedidoRegistrarCupomEPublicarEvento() {
            Usuario usuario = novoUsuario();

            Pedido pedido = novoPedidoComItem(
                    usuario,
                    2,
                    20
            ).pedido();

            Cupom cupom = novoCupom(
                    "DESC10",
                    "0.10"
            );

            pedido.aplicarCupom(cupom);

            mockPedidoDoUsuario(
                    usuario,
                    pedido
            );

            PedidoResponseDTO resposta =
                    pedidoService.pagarPedido(
                            ID_PEDIDO,
                            usuario
                    );

            assertAll(
                    () -> assertEquals(
                            StatusPedido.PAGO,
                            resposta.status()
                    ),
                    () -> assertBigDecimal(
                            "180.00",
                            resposta.valorFinal()
                    ),
                    () -> assertEquals(
                            1,
                            cupom.getQuantidadeDeUso()
                    )
            );

            PedidoPagoEvent evento =
                    capturarEvento(PedidoPagoEvent.class);

            assertAll(
                    () -> assertEquals(
                            ID_PEDIDO,
                            evento.idPedido()
                    ),
                    () -> assertEquals(
                            ID_USUARIO,
                            evento.idUsuario()
                    ),
                    () -> assertBigDecimal(
                            "180.00",
                            evento.valorFinal()
                    )
            );
        }

        @Test
        void naoDevePagarPedidoVazio() {
            Usuario usuario = novoUsuario();

            mockPedidoDoUsuario(
                    usuario,
                    novoPedidoVazio(usuario)
            );

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pedidoService.pagarPedido(
                            ID_PEDIDO,
                            usuario
                    )
            );

            assertEquals(
                    "Não é possível iniciar pagamento de um pedido sem itens.",
                    excecao.getMessage()
            );

            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    class TransicoesAdministrativas {

        @Test
        void deveEnviarPedidoPagoEPublicarEvento() {
            Pedido pedido = novoPedidoPago(
                    novoUsuario()
            );

            mockPedidoAdministrativo(pedido);

            PedidoResponseDTO resposta =
                    pedidoService.enviarPedido(ID_PEDIDO);

            assertEquals(
                    StatusPedido.ENVIADO,
                    resposta.status()
            );

            PedidoEnviadoEvent evento =
                    capturarEvento(PedidoEnviadoEvent.class);

            assertAll(
                    () -> assertEquals(
                            ID_PEDIDO,
                            evento.idPedido()
                    ),
                    () -> assertEquals(
                            ID_USUARIO,
                            evento.idUsuario()
                    ),
                    () -> assertEquals(
                            StatusPedido.ENVIADO,
                            evento.statusNovo()
                    )
            );
        }

        @Test
        void deveEntregarPedidoEnviadoEPublicarEvento() {
            Pedido pedido = novoPedidoPago(
                    novoUsuario()
            );

            pedido.enviar(new EstadoPago());

            mockPedidoAdministrativo(pedido);

            PedidoResponseDTO resposta =
                    pedidoService.entregarPedido(ID_PEDIDO);

            assertEquals(
                    StatusPedido.ENTREGUE,
                    resposta.status()
            );

            PedidoEntregueEvent evento =
                    capturarEvento(PedidoEntregueEvent.class);

            assertAll(
                    () -> assertEquals(
                            ID_PEDIDO,
                            evento.idPedido()
                    ),
                    () -> assertEquals(
                            ID_USUARIO,
                            evento.idUsuario()
                    ),
                    () -> assertBigDecimal(
                            "200.00",
                            evento.valorFinal()
                    )
            );
        }

        @Test
        void naoDeveEnviarPedidoCriado() {
            Usuario usuario = novoUsuario();

            Pedido pedido = novoPedidoComItem(
                    usuario,
                    2,
                    20
            ).pedido();

            mockPedidoAdministrativo(pedido);

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pedidoService.enviarPedido(
                            ID_PEDIDO
                    )
            );

            assertEquals(
                    "Pedido com status CRIADO não pode ser enviado.",
                    excecao.getMessage()
            );

            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    class CancelamentoEEstorno {

        @Test
        void deveCancelarPedidoCriadoDevolverEstoqueEPublicarEvento() {
            Usuario usuario = novoUsuario();

            PedidoComProduto fixture = novoPedidoComItem(
                    usuario,
                    2,
                    20
            );

            mockPedidoDoUsuario(
                    usuario,
                    fixture.pedido()
            );

            PedidoResponseDTO resposta =
                    pedidoService.cancelarPedido(
                            ID_PEDIDO,
                            usuario
                    );

            assertAll(
                    () -> assertEquals(
                            StatusPedido.CANCELADO,
                            resposta.status()
                    ),
                    () -> assertEquals(
                            20,
                            fixture.produto().getEstoque()
                    )
            );

            PedidoCanceladoEvent evento =
                    capturarEvento(PedidoCanceladoEvent.class);

            assertAll(
                    () -> assertEquals(
                            StatusPedido.CRIADO,
                            evento.statusAnterior()
                    ),
                    () -> assertEquals(
                            StatusPedido.CANCELADO,
                            evento.statusNovo()
                    )
            );
        }

        @Test
        void deveSolicitarCancelamentoDePedidoPagoSemDevolverEstoque() {
            Usuario usuario = novoUsuario();

            PedidoComProduto fixture = novoPedidoComItem(
                    usuario,
                    2,
                    20
            );

            fixture.pedido().pagar(new EstadoCriado());

            mockPedidoDoUsuario(
                    usuario,
                    fixture.pedido()
            );

            PedidoResponseDTO resposta =
                    pedidoService.cancelarPedido(
                            ID_PEDIDO,
                            usuario
                    );

            assertAll(
                    () -> assertEquals(
                            StatusPedido.CANCELAMENTO_SOLICITADO,
                            resposta.status()
                    ),
                    () -> assertEquals(
                            18,
                            fixture.produto().getEstoque()
                    )
            );

            PedidoCanceladoEvent evento =
                    capturarEvento(PedidoCanceladoEvent.class);

            assertAll(
                    () -> assertEquals(
                            StatusPedido.PAGO,
                            evento.statusAnterior()
                    ),
                    () -> assertEquals(
                            StatusPedido.CANCELAMENTO_SOLICITADO,
                            evento.statusNovo()
                    )
            );
        }

        @Test
        void deveEstornarPedidoDevolverEstoqueEPublicarEvento() {
            Usuario usuario = novoUsuario();

            PedidoComProduto fixture = novoPedidoComItem(
                    usuario,
                    2,
                    20
            );

            fixture.pedido().pagar(new EstadoCriado());
            fixture.pedido().cancelar(new EstadoPago());

            mockPedidoAdministrativo(fixture.pedido());

            PedidoResponseDTO resposta =
                    pedidoService.estornarPedido(
                            ID_PEDIDO
                    );

            assertAll(
                    () -> assertEquals(
                            StatusPedido.ESTORNADO,
                            resposta.status()
                    ),
                    () -> assertEquals(
                            20,
                            fixture.produto().getEstoque()
                    )
            );

            PedidoEstornadoEvent evento =
                    capturarEvento(PedidoEstornadoEvent.class);

            assertAll(
                    () -> assertEquals(
                            ID_PEDIDO,
                            evento.idPedido()
                    ),
                    () -> assertEquals(
                            ID_USUARIO,
                            evento.idUsuario()
                    ),
                    () -> assertBigDecimal(
                            "200.00",
                            evento.valorFinal()
                    )
            );
        }
    }

    @Nested
    class Checkout {

        @Test
        void deveMarcarPedidoCriadoComoPago() {
            Pedido pedido = novoPedidoComItem(
                    novoUsuario(),
                    2,
                    20
            ).pedido();

            pedidoService.marcarPedidoComoPagoAposPagamento(
                    pedido
            );

            assertEquals(
                    StatusPedido.PAGO,
                    pedido.getStatus()
            );

            verify(eventPublisher)
                    .publishEvent(
                            any(PedidoPagoEvent.class)
                    );
        }

        @Test
        void deveConfirmarPedidoAguardandoPagamentoComoPago() {
            Pedido pedido = novoPedidoComItem(
                    novoUsuario(),
                    2,
                    20
            ).pedido();

            pedido.aguardarPagamento(
                    new EstadoCriado()
            );

            pedidoService.marcarPedidoComoPagoAposPagamento(
                    pedido
            );

            assertEquals(
                    StatusPedido.PAGO,
                    pedido.getStatus()
            );

            verify(eventPublisher)
                    .publishEvent(
                            any(PedidoPagoEvent.class)
                    );
        }

        @Test
        void naoDeveMarcarPedidoPagoNovamente() {
            Pedido pedido = novoPedidoPago(
                    novoUsuario()
            );

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pedidoService
                            .marcarPedidoComoPagoAposPagamento(
                                    pedido
                            )
            );

            assertEquals(
                    "Pedido com status PAGO não pode ser marcado como pago.",
                    excecao.getMessage()
            );

            verifyNoInteractions(eventPublisher);
        }

        @Test
        void deveMarcarPedidoCriadoComoAguardandoPagamento() {
            Pedido pedido = novoPedidoComItem(
                    novoUsuario(),
                    2,
                    20
            ).pedido();

            pedidoService.marcarPedidoComoAguardandoPagamento(
                    pedido
            );

            assertEquals(
                    StatusPedido.AGUARDANDO_PAGAMENTO,
                    pedido.getStatus()
            );

            verifyNoInteractions(eventPublisher);
        }
    }

    private void mockPedidoDoUsuario(
            Usuario usuario,
            Pedido pedido) {
        when(
                pedidoRepository.findByIdAndUsuario(
                        ID_PEDIDO,
                        usuario
                )
        ).thenReturn(Optional.of(pedido));
    }

    private void mockPedidoAdministrativo(
            Pedido pedido) {
        when(
                pedidoRepository.findById(
                        ID_PEDIDO
                )
        ).thenReturn(Optional.of(pedido));
    }

    private void configurarSaveAndFlush() {
        when(
                pedidoRepository.saveAndFlush(
                        any(Pedido.class)
                )
        ).thenAnswer(invocation -> {
            Pedido pedido =
                    invocation.getArgument(0);

            if (pedido.getId() == null) {
                definirId(
                        pedido,
                        ID_PEDIDO
                );
            }

            pedido.getItens().forEach(item -> {
                if (item.getId() == null) {
                    definirId(
                            item,
                            ID_ITEM
                    );
                }
            });

            return pedido;
        });
    }

    private Usuario novoUsuario() {
        Usuario usuario = new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );

        definirId(
                usuario,
                ID_USUARIO
        );

        return usuario;
    }

    private Pedido novoPedidoVazio(
            Usuario usuario) {
        Pedido pedido = new Pedido(usuario);

        definirId(
                pedido,
                ID_PEDIDO
        );

        return pedido;
    }

    private PedidoComProduto novoPedidoComItem(
            Usuario usuario,
            int quantidade,
            int estoqueInicial) {
        Produto produto = novoProduto(
                estoqueInicial
        );

        Pedido pedido = novoPedidoVazio(
                usuario
        );

        pedido.adicionarItem(
                produto,
                quantidade
        );

        definirId(
                pedido.getItens().getFirst(),
                ID_ITEM
        );

        return new PedidoComProduto(
                pedido,
                produto
        );
    }

    private Pedido novoPedidoPago(
            Usuario usuario) {
        Pedido pedido = novoPedidoComItem(
                usuario,
                2,
                20
        ).pedido();

        pedido.pagar(
                new EstadoCriado()
        );

        return pedido;
    }

    private Produto novoProduto(
            int estoque) {
        Produto produto = new Produto(
                "Mouse",
                "Mouse sem fio",
                new BigDecimal("100.00"),
                estoque
        );

        definirId(
                produto,
                ID_PRODUTO
        );

        return produto;
    }

    private Cupom novoCupom(
            String codigo,
            String percentual) {
        return new Cupom(
                codigo,
                new BigDecimal(percentual),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                100
        );
    }

    private <T> T capturarEvento(
            Class<T> tipoEvento) {
        ArgumentCaptor<T> captor = ArgumentCaptor.forClass(tipoEvento);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }

    private void definirId(
            Object objeto,
            Long id) {
        ReflectionTestUtils.setField(
                objeto,
                "id",
                id
        );
    }

    private void assertBigDecimal(
            String esperado,
            BigDecimal atual) {
        assertEquals(
                0,
                new BigDecimal(esperado)
                        .compareTo(atual)
        );
    }

    private record PedidoComProduto(
            Pedido pedido,
            Produto produto) {
    }
}