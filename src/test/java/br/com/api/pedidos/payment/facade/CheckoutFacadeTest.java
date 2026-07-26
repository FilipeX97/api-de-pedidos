package br.com.api.pedidos.payment.facade;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.service.PedidoService;
import br.com.api.pedidos.order.state.StatusPedido;
import br.com.api.pedidos.payment.dto.PagamentoRequestDTO;
import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.service.PagamentoService;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutFacadeTest {

    private static final Long ID_PEDIDO = 10L;
    private static final Long ID_PAGAMENTO = 20L;

    @Mock
    private PedidoService pedidoService;

    @Mock
    private PagamentoService pagamentoService;

    @InjectMocks
    private CheckoutFacade checkoutFacade;

    @Nested
    class ProcessamentoDoPagamento {

        @Test
        void deveMarcarPedidoComoPagoQuandoPagamentoForAprovado() {
            Usuario usuario = novoUsuario();
            Pedido pedido = novoPedidoComItem(
                    usuario,
                    StatusPedido.CRIADO
            );

            Pagamento pagamento = novoPagamento(
                    pedido,
                    FormaPagamento.CARTAO_CREDITO,
                    StatusPagamento.APROVADO
            );

            PagamentoRequestDTO request =
                    new PagamentoRequestDTO(
                            FormaPagamento.CARTAO_CREDITO
                    );

            when(
                    pedidoService.buscarPedidoDoUsuarioParaCheckout(
                            ID_PEDIDO,
                            usuario
                    )
            ).thenReturn(pedido);

            when(
                    pagamentoService.processarPagamento(
                            pedido,
                            FormaPagamento.CARTAO_CREDITO
                    )
            ).thenReturn(pagamento);

            PagamentoResponseDTO resposta =
                    checkoutFacade.processarPagamento(
                            ID_PEDIDO,
                            usuario,
                            request
                    );

            assertAll(
                    () -> assertEquals(
                            ID_PAGAMENTO,
                            resposta.idPagamento()
                    ),
                    () -> assertEquals(
                            ID_PEDIDO,
                            resposta.idPedido()
                    ),
                    () -> assertBigDecimalEquals(
                            "200.00",
                            resposta.valor()
                    ),
                    () -> assertEquals(
                            FormaPagamento.CARTAO_CREDITO,
                            resposta.formaPagamento()
                    ),
                    () -> assertEquals(
                            StatusPagamento.APROVADO,
                            resposta.statusPagamento()
                    ),
                    () -> assertEquals(
                            "CARD-123",
                            resposta.codigoTransacao()
                    ),
                    () -> assertEquals(
                            "Pagamento autorizado",
                            resposta.mensagem()
                    ),
                    () -> assertNotNull(
                            resposta.dataCriacao()
                    )
            );

            InOrder ordem = inOrder(
                    pedidoService,
                    pagamentoService
            );

            ordem.verify(pedidoService)
                    .buscarPedidoDoUsuarioParaCheckout(
                            ID_PEDIDO,
                            usuario
                    );

            ordem.verify(pedidoService)
                    .recalcularPedidoParaCheckout(pedido);

            ordem.verify(pagamentoService)
                    .processarPagamento(
                            pedido,
                            FormaPagamento.CARTAO_CREDITO
                    );

            ordem.verify(pedidoService)
                    .marcarPedidoComoPagoAposPagamento(pedido);

            verify(
                    pedidoService,
                    never()
            ).marcarPedidoComoAguardandoPagamento(any());
        }

        @Test
        void deveMarcarPedidoComoAguardandoPagamentoQuandoPixForPendente() {
            Usuario usuario = novoUsuario();

            Pedido pedido = novoPedidoComItem(
                    usuario,
                    StatusPedido.CRIADO
            );

            Pagamento pagamento = novoPagamento(
                    pedido,
                    FormaPagamento.PIX,
                    StatusPagamento.PENDENTE
            );

            PagamentoRequestDTO request =
                    new PagamentoRequestDTO(
                            FormaPagamento.PIX
                    );

            when(
                    pedidoService.buscarPedidoDoUsuarioParaCheckout(
                            ID_PEDIDO,
                            usuario
                    )
            ).thenReturn(pedido);

            when(
                    pagamentoService.processarPagamento(
                            pedido,
                            FormaPagamento.PIX
                    )
            ).thenReturn(pagamento);

            PagamentoResponseDTO resposta =
                    checkoutFacade.processarPagamento(
                            ID_PEDIDO,
                            usuario,
                            request
                    );

            assertAll(
                    () -> assertEquals(
                            StatusPagamento.PENDENTE,
                            resposta.statusPagamento()
                    ),
                    () -> assertEquals(
                            FormaPagamento.PIX,
                            resposta.formaPagamento()
                    ),
                    () -> assertEquals(
                            "PIX-123",
                            resposta.codigoTransacao()
                    ),
                    () -> assertEquals(
                            "Pagamento aguardando confirmação",
                            resposta.mensagem()
                    )
            );

            verify(pedidoService)
                    .recalcularPedidoParaCheckout(pedido);

            verify(pagamentoService)
                    .processarPagamento(
                            pedido,
                            FormaPagamento.PIX
                    );

            verify(pedidoService)
                    .marcarPedidoComoAguardandoPagamento(pedido);

            verify(
                    pedidoService,
                    never()
            ).marcarPedidoComoPagoAposPagamento(any());
        }

        @Test
        void deveMarcarPedidoComoAguardandoPagamentoQuandoBoletoForPendente() {
            Usuario usuario = novoUsuario();

            Pedido pedido = novoPedidoComItem(
                    usuario,
                    StatusPedido.CRIADO
            );

            Pagamento pagamento = novoPagamento(
                    pedido,
                    FormaPagamento.BOLETO,
                    StatusPagamento.PENDENTE
            );

            PagamentoRequestDTO request =
                    new PagamentoRequestDTO(
                            FormaPagamento.BOLETO
                    );

            when(
                    pedidoService.buscarPedidoDoUsuarioParaCheckout(
                            ID_PEDIDO,
                            usuario
                    )
            ).thenReturn(pedido);

            when(
                    pagamentoService.processarPagamento(
                            pedido,
                            FormaPagamento.BOLETO
                    )
            ).thenReturn(pagamento);

            PagamentoResponseDTO resposta =
                    checkoutFacade.processarPagamento(
                            ID_PEDIDO,
                            usuario,
                            request
                    );

            assertAll(
                    () -> assertEquals(
                            StatusPagamento.PENDENTE,
                            resposta.statusPagamento()
                    ),
                    () -> assertEquals(
                            FormaPagamento.BOLETO,
                            resposta.formaPagamento()
                    ),
                    () -> assertEquals(
                            "BOL-123",
                            resposta.codigoTransacao()
                    )
            );

            verify(pedidoService)
                    .marcarPedidoComoAguardandoPagamento(pedido);

            verify(
                    pedidoService,
                    never()
            ).marcarPedidoComoPagoAposPagamento(any());
        }

        @Test
        void naoDeveAlterarPedidoQuandoPagamentoForRecusado() {
            Usuario usuario = novoUsuario();

            Pedido pedido = novoPedidoComItem(
                    usuario,
                    StatusPedido.CRIADO
            );

            Pagamento pagamento = novoPagamento(
                    pedido,
                    FormaPagamento.CARTAO_CREDITO,
                    StatusPagamento.RECUSADO
            );

            PagamentoRequestDTO request =
                    new PagamentoRequestDTO(
                            FormaPagamento.CARTAO_CREDITO
                    );

            when(
                    pedidoService.buscarPedidoDoUsuarioParaCheckout(
                            ID_PEDIDO,
                            usuario
                    )
            ).thenReturn(pedido);

            when(
                    pagamentoService.processarPagamento(
                            pedido,
                            FormaPagamento.CARTAO_CREDITO
                    )
            ).thenReturn(pagamento);

            PagamentoResponseDTO resposta =
                    checkoutFacade.processarPagamento(
                            ID_PEDIDO,
                            usuario,
                            request
                    );

            assertAll(
                    () -> assertEquals(
                            StatusPagamento.RECUSADO,
                            resposta.statusPagamento()
                    ),
                    () -> assertEquals(
                            "CARD-123",
                            resposta.codigoTransacao()
                    ),
                    () -> assertEquals(
                            "Pagamento recusado",
                            resposta.mensagem()
                    )
            );

            verify(pedidoService)
                    .recalcularPedidoParaCheckout(pedido);

            verify(pagamentoService)
                    .processarPagamento(
                            pedido,
                            FormaPagamento.CARTAO_CREDITO
                    );

            verify(
                    pedidoService,
                    never()
            ).marcarPedidoComoPagoAposPagamento(any());

            verify(
                    pedidoService,
                    never()
            ).marcarPedidoComoAguardandoPagamento(any());
        }

        @Test
        void naoDeveProcessarPagamentoSemRequest() {
            Usuario usuario = novoUsuario();

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> checkoutFacade.processarPagamento(
                            ID_PEDIDO,
                            usuario,
                            null
                    )
            );

            assertEquals(
                    "Dados do pagamento são obrigatórios",
                    excecao.getMessage()
            );

            verifyNoInteractions(
                    pedidoService,
                    pagamentoService
            );
        }

        @Test
        void naoDeveProcessarPagamentoSemFormaDePagamento() {
            Usuario usuario = novoUsuario();

            PagamentoRequestDTO request =
                    new PagamentoRequestDTO(null);

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> checkoutFacade.processarPagamento(
                            ID_PEDIDO,
                            usuario,
                            request
                    )
            );

            assertEquals(
                    "Forma de pagamento é obrigatória",
                    excecao.getMessage()
            );

            verifyNoInteractions(
                    pedidoService,
                    pagamentoService
            );
        }

        @Test
        void naoDeveProcessarPedidoQueNaoEstaCriado() {
            Usuario usuario = novoUsuario();

            Pedido pedido = novoPedidoComItem(
                    usuario,
                    StatusPedido.PAGO
            );

            PagamentoRequestDTO request =
                    new PagamentoRequestDTO(
                            FormaPagamento.PIX
                    );

            when(
                    pedidoService.buscarPedidoDoUsuarioParaCheckout(
                            ID_PEDIDO,
                            usuario
                    )
            ).thenReturn(pedido);

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> checkoutFacade.processarPagamento(
                            ID_PEDIDO,
                            usuario,
                            request
                    )
            );

            assertEquals(
                    "Somente pedido com status CRIADO pode iniciar pagamento. "
                            + "Status atual: PAGO",
                    excecao.getMessage()
            );

            verify(pedidoService)
                    .buscarPedidoDoUsuarioParaCheckout(
                            ID_PEDIDO,
                            usuario
                    );

            verify(
                    pedidoService,
                    never()
            ).recalcularPedidoParaCheckout(any());

            verifyNoInteractions(pagamentoService);
        }

        @Test
        void naoDeveProcessarPedidoVazio() {
            Usuario usuario = novoUsuario();
            Pedido pedido = novoPedidoVazio(usuario);

            PagamentoRequestDTO request =
                    new PagamentoRequestDTO(
                            FormaPagamento.PIX
                    );

            when(
                    pedidoService.buscarPedidoDoUsuarioParaCheckout(
                            ID_PEDIDO,
                            usuario
                    )
            ).thenReturn(pedido);

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> checkoutFacade.processarPagamento(
                            ID_PEDIDO,
                            usuario,
                            request
                    )
            );

            assertEquals(
                    "Não é possível pagar um pedido sem itens",
                    excecao.getMessage()
            );

            verify(
                    pedidoService,
                    never()
            ).recalcularPedidoParaCheckout(any());

            verifyNoInteractions(pagamentoService);
        }

        @Test
        void naoDeveAceitarStatusDePagamentoNaoTratadoNoCheckout() {
            Usuario usuario = novoUsuario();

            Pedido pedido = novoPedidoComItem(
                    usuario,
                    StatusPedido.CRIADO
            );

            Pagamento pagamento = novoPagamento(
                    pedido,
                    FormaPagamento.PIX,
                    StatusPagamento.CANCELADO
            );

            PagamentoRequestDTO request =
                    new PagamentoRequestDTO(
                            FormaPagamento.PIX
                    );

            when(
                    pedidoService.buscarPedidoDoUsuarioParaCheckout(
                            ID_PEDIDO,
                            usuario
                    )
            ).thenReturn(pedido);

            when(
                    pagamentoService.processarPagamento(
                            pedido,
                            FormaPagamento.PIX
                    )
            ).thenReturn(pagamento);

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> checkoutFacade.processarPagamento(
                            ID_PEDIDO,
                            usuario,
                            request
                    )
            );

            assertEquals(
                    "Status de pagamento não tratado no checkout: CANCELADO",
                    excecao.getMessage()
            );

            verify(
                    pedidoService,
                    never()
            ).marcarPedidoComoPagoAposPagamento(any());

            verify(
                    pedidoService,
                    never()
            ).marcarPedidoComoAguardandoPagamento(any());
        }
    }

    @Nested
    class ProcessamentoDoWebhook {

        @Test
        void deveMarcarPedidoComoPagoQuandoWebhookAprovarPagamentoPendente() {
            Pedido pedido = novoPedidoComItem(
                    novoUsuario(),
                    StatusPedido.AGUARDANDO_PAGAMENTO
            );

            Pagamento pagamento = novoPagamento(
                    pedido,
                    FormaPagamento.PIX,
                    StatusPagamento.APROVADO
            );

            when(
                    pagamentoService.processarConfirmacaoDoGateway(
                            "PIX-123"
                    )
            ).thenReturn(pagamento);

            PagamentoResponseDTO resposta =
                    checkoutFacade.processarWebhookPagamento(
                            "PIX-123"
                    );

            assertAll(
                    () -> assertEquals(
                            StatusPagamento.APROVADO,
                            resposta.statusPagamento()
                    ),
                    () -> assertEquals(
                            "PIX-123",
                            resposta.codigoTransacao()
                    ),
                    () -> assertEquals(
                            ID_PEDIDO,
                            resposta.idPedido()
                    )
            );

            verify(pedidoService).marcarPedidoComoPagoAposPagamento(pedido);
        }

        @Test
        void naoDeveMarcarNovamentePedidoQueJaEstaPago() {
            Pedido pedido = novoPedidoComItem(
                    novoUsuario(),
                    StatusPedido.PAGO
            );

            Pagamento pagamento = novoPagamento(
                    pedido,
                    FormaPagamento.PIX,
                    StatusPagamento.APROVADO
            );

            when(
                    pagamentoService.processarConfirmacaoDoGateway(
                            "PIX-123"
                    )
            ).thenReturn(pagamento);

            PagamentoResponseDTO resposta =
                    checkoutFacade.processarWebhookPagamento(
                            "PIX-123"
                    );

            assertEquals(
                    StatusPagamento.APROVADO,
                    resposta.statusPagamento()
            );

            verify(
                    pedidoService,
                    never()
            ).marcarPedidoComoPagoAposPagamento(any());
        }

        @Test
        void naoDeveConfirmarWebhookAprovadoParaPedidoCriado() {
            Pedido pedido = novoPedidoComItem(
                    novoUsuario(),
                    StatusPedido.CRIADO
            );

            Pagamento pagamento = novoPagamento(
                    pedido,
                    FormaPagamento.PIX,
                    StatusPagamento.APROVADO
            );

            when(
                    pagamentoService.processarConfirmacaoDoGateway(
                            "PIX-123"
                    )
            ).thenReturn(pagamento);

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> checkoutFacade.processarWebhookPagamento(
                            "PIX-123"
                    )
            );

            assertEquals(
                    "Somente pedido aguardando pagamento pode ser confirmado "
                            + "pelo webhook. Status atual: CRIADO",
                    excecao.getMessage()
            );

            verify(
                    pedidoService,
                    never()
            ).marcarPedidoComoPagoAposPagamento(any());
        }

        @Test
        void deveManterPedidoQuandoPagamentoContinuarPendente() {
            Pedido pedido = novoPedidoComItem(
                    novoUsuario(),
                    StatusPedido.AGUARDANDO_PAGAMENTO
            );

            Pagamento pagamento = novoPagamento(
                    pedido,
                    FormaPagamento.PIX,
                    StatusPagamento.PENDENTE
            );

            when(
                    pagamentoService.processarConfirmacaoDoGateway(
                            "PIX-123"
                    )
            ).thenReturn(pagamento);

            PagamentoResponseDTO resposta =
                    checkoutFacade.processarWebhookPagamento(
                            "PIX-123"
                    );

            assertEquals(
                    StatusPagamento.PENDENTE,
                    resposta.statusPagamento()
            );

            verify(
                    pedidoService,
                    never()
            ).marcarPedidoComoPagoAposPagamento(any());
        }

        @Test
        void deveManterPedidoQuandoPagamentoForRecusadoPeloWebhook() {
            Pedido pedido = novoPedidoComItem(
                    novoUsuario(),
                    StatusPedido.AGUARDANDO_PAGAMENTO
            );

            Pagamento pagamento = novoPagamento(
                    pedido,
                    FormaPagamento.PIX,
                    StatusPagamento.RECUSADO
            );

            when(
                    pagamentoService.processarConfirmacaoDoGateway(
                            "PIX-123"
                    )
            ).thenReturn(pagamento);

            PagamentoResponseDTO resposta =
                    checkoutFacade.processarWebhookPagamento(
                            "PIX-123"
                    );

            assertEquals(
                    StatusPagamento.RECUSADO,
                    resposta.statusPagamento()
            );

            verify(
                    pedidoService,
                    never()
            ).marcarPedidoComoPagoAposPagamento(any());
        }

        @Test
        void naoDeveAceitarStatusNaoTratadoNoWebhook() {
            Pedido pedido = novoPedidoComItem(
                    novoUsuario(),
                    StatusPedido.AGUARDANDO_PAGAMENTO
            );

            Pagamento pagamento = novoPagamento(
                    pedido,
                    FormaPagamento.PIX,
                    StatusPagamento.CANCELADO
            );

            when(
                    pagamentoService.processarConfirmacaoDoGateway(
                            "PIX-123"
                    )
            ).thenReturn(pagamento);

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> checkoutFacade.processarWebhookPagamento(
                            "PIX-123"
                    )
            );

            assertEquals(
                    "Status de pagamento não tratado no webhook: CANCELADO",
                    excecao.getMessage()
            );

            verify(
                    pedidoService,
                    never()
            ).marcarPedidoComoPagoAposPagamento(any());
        }
    }

    @Nested
    class Consultas {

        @Test
        void deveListarPagamentosDoPedidoDoUsuario() {
            Usuario usuario = novoUsuario();

            Pedido pedido = novoPedidoComItem(
                    usuario,
                    StatusPedido.CRIADO
            );

            PagamentoResponseDTO pagamentoPix =
                    new PagamentoResponseDTO(
                            20L,
                            ID_PEDIDO,
                            new BigDecimal("200.00"),
                            FormaPagamento.PIX,
                            StatusPagamento.PENDENTE,
                            "PIX-123",
                            "Pagamento aguardando confirmação",
                            LocalDateTime.now()
                    );

            PagamentoResponseDTO pagamentoCartao =
                    new PagamentoResponseDTO(
                            21L,
                            ID_PEDIDO,
                            new BigDecimal("200.00"),
                            FormaPagamento.CARTAO_CREDITO,
                            StatusPagamento.RECUSADO,
                            "CARD-123",
                            "Pagamento recusado",
                            LocalDateTime.now()
                    );

            List<PagamentoResponseDTO> pagamentos =
                    List.of(
                            pagamentoPix,
                            pagamentoCartao
                    );

            when(
                    pedidoService.buscarPedidoDoUsuarioParaCheckout(
                            ID_PEDIDO,
                            usuario
                    )
            ).thenReturn(pedido);

            when(
                    pagamentoService.listarPagamentosDoPedido(
                            ID_PEDIDO
                    )
            ).thenReturn(pagamentos);

            List<PagamentoResponseDTO> resultado =
                    checkoutFacade.listarPagamentosDoPedido(
                            ID_PEDIDO,
                            usuario
                    );

            assertSame(
                    pagamentos,
                    resultado
            );

            assertEquals(
                    2,
                    resultado.size()
            );

            verify(pedidoService)
                    .buscarPedidoDoUsuarioParaCheckout(
                            ID_PEDIDO,
                            usuario
                    );

            verify(pagamentoService)
                    .listarPagamentosDoPedido(ID_PEDIDO);
        }

        @Test
        void deveBuscarPagamentoPeloCodigoDaTransacao() {
            Pedido pedido = novoPedidoComItem(
                    novoUsuario(),
                    StatusPedido.AGUARDANDO_PAGAMENTO
            );

            Pagamento pagamento = novoPagamento(
                    pedido,
                    FormaPagamento.PIX,
                    StatusPagamento.PENDENTE
            );

            when(
                    pagamentoService.buscarPagamentoPorCodigoTransacao(
                            "PIX-123"
                    )
            ).thenReturn(pagamento);

            PagamentoResponseDTO resposta =
                    checkoutFacade.buscarPagamentoPorCodigoTransacao(
                            "PIX-123"
                    );

            assertAll(
                    () -> assertEquals(
                            ID_PAGAMENTO,
                            resposta.idPagamento()
                    ),
                    () -> assertEquals(
                            ID_PEDIDO,
                            resposta.idPedido()
                    ),
                    () -> assertEquals(
                            FormaPagamento.PIX,
                            resposta.formaPagamento()
                    ),
                    () -> assertEquals(
                            StatusPagamento.PENDENTE,
                            resposta.statusPagamento()
                    ),
                    () -> assertEquals(
                            "PIX-123",
                            resposta.codigoTransacao()
                    )
            );

            verify(pagamentoService)
                    .buscarPagamentoPorCodigoTransacao(
                            "PIX-123"
                    );
        }
    }

    private Usuario novoUsuario() {
        Usuario usuario = new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );

        ReflectionTestUtils.setField(
                usuario,
                "id",
                1L
        );

        return usuario;
    }

    private Pedido novoPedidoVazio(
            Usuario usuario) {
        Pedido pedido = new Pedido(usuario);

        ReflectionTestUtils.setField(
                pedido,
                "id",
                ID_PEDIDO
        );

        return pedido;
    }

    private Pedido novoPedidoComItem(
            Usuario usuario,
            StatusPedido status) {
        Produto produto = new Produto(
                "Mouse",
                "Mouse sem fio",
                new BigDecimal("100.00"),
                20
        );

        Pedido pedido = new Pedido(usuario);

        pedido.adicionarItem(
                produto,
                2
        );

        ReflectionTestUtils.setField(
                pedido,
                "id",
                ID_PEDIDO
        );

        ReflectionTestUtils.setField(
                pedido,
                "status",
                status
        );

        return pedido;
    }

    private Pagamento novoPagamento(
            Pedido pedido,
            FormaPagamento formaPagamento,
            StatusPagamento statusPagamento) {

        Pagamento pagamento = new Pagamento(
                pedido,
                pedido.getValorFinal(),
                formaPagamento
        );

        ReflectionTestUtils.setField(
                pagamento,
                "id",
                ID_PAGAMENTO
        );

        switch (statusPagamento) {
            case APROVADO -> pagamento.aprovar(
                    codigoTransacao(formaPagamento),
                    "Pagamento autorizado"
            );

            case RECUSADO -> pagamento.recusar(
                    codigoTransacao(formaPagamento),
                    "Pagamento recusado"
            );

            case PENDENTE -> pagamento.deixarPendente(
                    codigoTransacao(formaPagamento),
                    "Pagamento aguardando confirmação"
            );

            case CANCELADO -> pagamento.cancelar(
                    "Pagamento cancelado"
            );

            case ESTORNADO -> {
                pagamento.aprovar(
                        codigoTransacao(formaPagamento),
                        "Pagamento autorizado"
                );

                pagamento.estornar(
                        "REFUND-123",
                        "Pagamento estornado"
                );
            }
        }

        return pagamento;
    }

    private String codigoTransacao(
            FormaPagamento formaPagamento) {

        return switch (formaPagamento) {
            case PIX -> "PIX-123";
            case BOLETO -> "BOL-123";
            case CARTAO_CREDITO -> "CARD-123";
        };
    }

    private void assertBigDecimalEquals(
            String esperado,
            BigDecimal atual) {
        assertEquals(
                0,
                new BigDecimal(esperado).compareTo(atual)
        );
    }
}