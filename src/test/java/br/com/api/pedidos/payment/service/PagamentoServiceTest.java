package br.com.api.pedidos.payment.service;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.payment.adapter.ResultadoPagamento;
import br.com.api.pedidos.payment.adapter.fake.GatewayPagamentoFakeConsulta;
import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.repository.PagamentoRepository;
import br.com.api.pedidos.payment.strategy.EstrategiaPagamento;
import br.com.api.pedidos.payment.strategy.EstrategiaPagamentoFactory;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private EstrategiaPagamentoFactory estrategiaPagamentoFactory;

    @Mock
    private GatewayPagamentoFakeConsulta gatewayPagamentoFakeConsulta;

    @Mock
    private EstrategiaPagamento estrategiaPagamento;

    @InjectMocks
    private PagamentoService pagamentoService;

    @Nested
    class ProcessamentoDoPagamento {

        @Test
        void deveProcessarPagamentoAprovado() {
            Pedido pedido = novoPedidoComItem();
            pedido.aplicarDesconto(
                    new BigDecimal("20.00")
            );

            ResultadoPagamento resultadoGateway =
                    new ResultadoPagamento(
                            StatusPagamento.APROVADO,
                            "CARD-APROVADO-123",
                            "Pagamento autorizado pela operadora"
                    );

            configurarPersistenciaRetornandoMesmoPagamento();

            when(
                    estrategiaPagamentoFactory.obter(
                            FormaPagamento.CARTAO_CREDITO
                    )
            ).thenReturn(estrategiaPagamento);

            when(
                    estrategiaPagamento.processar(
                            any(Pagamento.class)
                    )
            ).thenReturn(resultadoGateway);

            Pagamento pagamento =
                    pagamentoService.processarPagamento(
                            pedido,
                            FormaPagamento.CARTAO_CREDITO
                    );

            assertAll(
                    () -> assertSame(
                            pedido,
                            pagamento.getPedido()
                    ),
                    () -> assertBigDecimalEquals(
                            "180.00",
                            pagamento.getValor()
                    ),
                    () -> assertEquals(
                            FormaPagamento.CARTAO_CREDITO,
                            pagamento.getFormaPagamento()
                    ),
                    () -> assertEquals(
                            StatusPagamento.APROVADO,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            "CARD-APROVADO-123",
                            pagamento.getCodigoTransacao()
                    ),
                    () -> assertEquals(
                            "Pagamento autorizado pela operadora",
                            pagamento.getMensagemRetorno()
                    )
            );

            verify(pagamentoRepository, times(2))
                    .saveAndFlush(pagamento);

            verify(estrategiaPagamentoFactory)
                    .obter(FormaPagamento.CARTAO_CREDITO);

            verify(estrategiaPagamento)
                    .processar(pagamento);

            verify(
                    gatewayPagamentoFakeConsulta,
                    never()
            ).registrarPagamentoPendente(any());
        }

        @Test
        void deveProcessarPagamentoRecusado() {
            Pedido pedido = novoPedidoComItem();

            ResultadoPagamento resultadoGateway =
                    new ResultadoPagamento(
                            StatusPagamento.RECUSADO,
                            "CARD-RECUSADO-123",
                            "Pagamento recusado pela operadora"
                    );

            configurarPersistenciaRetornandoMesmoPagamento();

            when(
                    estrategiaPagamentoFactory.obter(
                            FormaPagamento.CARTAO_CREDITO
                    )
            ).thenReturn(estrategiaPagamento);

            when(
                    estrategiaPagamento.processar(
                            any(Pagamento.class)
                    )
            ).thenReturn(resultadoGateway);

            Pagamento pagamento =
                    pagamentoService.processarPagamento(
                            pedido,
                            FormaPagamento.CARTAO_CREDITO
                    );

            assertAll(
                    () -> assertEquals(
                            StatusPagamento.RECUSADO,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            "CARD-RECUSADO-123",
                            pagamento.getCodigoTransacao()
                    ),
                    () -> assertEquals(
                            "Pagamento recusado pela operadora",
                            pagamento.getMensagemRetorno()
                    ),
                    () -> assertBigDecimalEquals(
                            "200.00",
                            pagamento.getValor()
                    )
            );

            verify(pagamentoRepository, times(2))
                    .saveAndFlush(pagamento);

            verify(
                    gatewayPagamentoFakeConsulta,
                    never()
            ).registrarPagamentoPendente(any());
        }

        @Test
        void deveProcessarPagamentoPendenteERegistrarNoGatewayFake() {
            Pedido pedido = novoPedidoComItem();

            ResultadoPagamento resultadoGateway =
                    new ResultadoPagamento(
                            StatusPagamento.PENDENTE,
                            "PIX-123",
                            "PIX gerado e aguardando pagamento"
                    );

            configurarPersistenciaRetornandoMesmoPagamento();

            when(
                    estrategiaPagamentoFactory.obter(
                            FormaPagamento.PIX
                    )
            ).thenReturn(estrategiaPagamento);

            when(
                    estrategiaPagamento.processar(
                            any(Pagamento.class)
                    )
            ).thenReturn(resultadoGateway);

            Pagamento pagamento =
                    pagamentoService.processarPagamento(
                            pedido,
                            FormaPagamento.PIX
                    );

            assertAll(
                    () -> assertEquals(
                            StatusPagamento.PENDENTE,
                            pagamento.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            FormaPagamento.PIX,
                            pagamento.getFormaPagamento()
                    ),
                    () -> assertEquals(
                            "PIX-123",
                            pagamento.getCodigoTransacao()
                    ),
                    () -> assertEquals(
                            "PIX gerado e aguardando pagamento",
                            pagamento.getMensagemRetorno()
                    )
            );

            verify(
                    gatewayPagamentoFakeConsulta
            ).registrarPagamentoPendente("PIX-123");

            verify(pagamentoRepository, times(2))
                    .saveAndFlush(pagamento);
        }

        @Test
        void naoDeveProcessarPagamentoSemPedido() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pagamentoService.processarPagamento(
                            null,
                            FormaPagamento.PIX
                    )
            );

            assertEquals(
                    "Pedido é obrigatório",
                    excecao.getMessage()
            );

            verifyNoInteractions(
                    pagamentoRepository,
                    estrategiaPagamentoFactory,
                    estrategiaPagamento,
                    gatewayPagamentoFakeConsulta
            );
        }

        @Test
        void naoDeveProcessarPagamentoDePedidoVazio() {
            Pedido pedido = novoPedidoVazio();

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pagamentoService.processarPagamento(
                            pedido,
                            FormaPagamento.PIX
                    )
            );

            assertEquals(
                    "Não é possível pagar um pedido sem itens",
                    excecao.getMessage()
            );

            verifyNoInteractions(
                    pagamentoRepository,
                    estrategiaPagamentoFactory,
                    estrategiaPagamento,
                    gatewayPagamentoFakeConsulta
            );
        }

        @Test
        void naoDeveProcessarPagamentoSemFormaDePagamento() {
            Pedido pedido = novoPedidoComItem();

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pagamentoService.processarPagamento(
                            pedido,
                            null
                    )
            );

            assertEquals(
                    "Forma de pagamento é obrigatória",
                    excecao.getMessage()
            );

            verifyNoInteractions(
                    pagamentoRepository,
                    estrategiaPagamentoFactory,
                    estrategiaPagamento,
                    gatewayPagamentoFakeConsulta
            );
        }

        @Test
        void naoDeveAceitarStatusDeResultadoNaoSuportado() {
            Pedido pedido = novoPedidoComItem();

            ResultadoPagamento resultadoGateway =
                    new ResultadoPagamento(
                            StatusPagamento.CANCELADO,
                            "TRANSACAO-123",
                            "Status inesperado"
                    );

            configurarPersistenciaRetornandoMesmoPagamento();

            when(
                    estrategiaPagamentoFactory.obter(
                            FormaPagamento.PIX
                    )
            ).thenReturn(estrategiaPagamento);

            when(
                    estrategiaPagamento.processar(
                            any(Pagamento.class)
                    )
            ).thenReturn(resultadoGateway);

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pagamentoService.processarPagamento(
                            pedido,
                            FormaPagamento.PIX
                    )
            );

            assertEquals(
                    "Status de pagamento não suportado: CANCELADO",
                    excecao.getMessage()
            );

            /*
             * O primeiro save ocorre antes da chamada ao gateway.
             * O segundo não ocorre porque o resultado é inválido.
             */
            verify(pagamentoRepository, times(1))
                    .saveAndFlush(any(Pagamento.class));

            verify(
                    gatewayPagamentoFakeConsulta,
                    never()
            ).registrarPagamentoPendente(any());
        }
    }

    @Nested
    class ConsultasDePagamento {

        @Test
        void deveListarPagamentosDoPedidoNaOrdemDoRepository() {
            Pedido pedido = novoPedidoComItem();
            definirId(pedido, 10L);

            Pagamento pix = new Pagamento(
                    pedido,
                    new BigDecimal("200.00"),
                    FormaPagamento.PIX
            );

            definirId(pix, 2L);

            pix.deixarPendente(
                    "PIX-123",
                    "PIX aguardando pagamento"
            );

            Pagamento cartao = new Pagamento(
                    pedido,
                    new BigDecimal("200.00"),
                    FormaPagamento.CARTAO_CREDITO
            );

            definirId(cartao, 1L);

            cartao.aprovar(
                    "CARD-123",
                    "Cartão aprovado"
            );

            when(
                    pagamentoRepository
                            .findAllByPedidoIdOrderByDataCriacaoDesc(10L)
            ).thenReturn(
                    List.of(
                            pix,
                            cartao
                    )
            );

            List<PagamentoResponseDTO> resultado =
                    pagamentoService.listarPagamentosDoPedido(10L);

            assertAll(
                    () -> assertEquals(
                            2,
                            resultado.size()
                    ),
                    () -> assertEquals(
                            2L,
                            resultado.get(0).idPagamento()
                    ),
                    () -> assertEquals(
                            10L,
                            resultado.get(0).idPedido()
                    ),
                    () -> assertEquals(
                            FormaPagamento.PIX,
                            resultado.get(0).formaPagamento()
                    ),
                    () -> assertEquals(
                            StatusPagamento.PENDENTE,
                            resultado.get(0).statusPagamento()
                    ),
                    () -> assertEquals(
                            "PIX-123",
                            resultado.get(0).codigoTransacao()
                    ),
                    () -> assertEquals(
                            1L,
                            resultado.get(1).idPagamento()
                    ),
                    () -> assertEquals(
                            FormaPagamento.CARTAO_CREDITO,
                            resultado.get(1).formaPagamento()
                    ),
                    () -> assertEquals(
                            StatusPagamento.APROVADO,
                            resultado.get(1).statusPagamento()
                    )
            );

            verify(
                    pagamentoRepository
            ).findAllByPedidoIdOrderByDataCriacaoDesc(10L);
        }

        @Test
        void deveBuscarPagamentoPertencenteAoPedido() {
            Pedido pedido = novoPedidoComItem();

            Pagamento pagamento = new Pagamento(
                    pedido,
                    new BigDecimal("200.00"),
                    FormaPagamento.PIX
            );

            when(
                    pagamentoRepository.findByIdAndPedidoId(
                            20L,
                            10L
                    )
            ).thenReturn(Optional.of(pagamento));

            Pagamento resultado =
                    pagamentoService.buscarPagamentoDoPedido(
                            10L,
                            20L
                    );

            assertSame(
                    pagamento,
                    resultado
            );
        }

        @Test
        void naoDeveBuscarPagamentoQueNaoPertenceAoPedido() {
            when(
                    pagamentoRepository.findByIdAndPedidoId(
                            20L,
                            10L
                    )
            ).thenReturn(Optional.empty());

            RuntimeException excecao = assertThrows(
                    RuntimeException.class,
                    () -> pagamentoService.buscarPagamentoDoPedido(
                            10L,
                            20L
                    )
            );

            assertEquals(
                    "Pagamento não encontrado para este pedido",
                    excecao.getMessage()
            );
        }

        @Test
        void deveBuscarPagamentoPeloCodigoDaTransacao() {
            Pagamento pagamento =
                    novoPagamentoPendente(
                            FormaPagamento.PIX,
                            "PIX-123"
                    );

            when(
                    pagamentoRepository.findByCodigoTransacao(
                            "PIX-123"
                    )
            ).thenReturn(Optional.of(pagamento));

            Pagamento resultado =
                    pagamentoService
                            .buscarPagamentoPorCodigoTransacao(
                                    "PIX-123"
                            );

            assertSame(
                    pagamento,
                    resultado
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        void naoDeveBuscarPagamentoSemCodigoDeTransacao(
                String codigoTransacao) {

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> pagamentoService
                            .buscarPagamentoPorCodigoTransacao(
                                    codigoTransacao
                            )
            );

            assertEquals(
                    "Código da transação é obrigatório",
                    excecao.getMessage()
            );

            verifyNoInteractions(pagamentoRepository);
        }

        @Test
        void naoDeveBuscarCodigoDeTransacaoInexistente() {
            when(
                    pagamentoRepository.findByCodigoTransacao(
                            "PIX-INEXISTENTE"
                    )
            ).thenReturn(Optional.empty());

            RuntimeException excecao = assertThrows(
                    RuntimeException.class,
                    () -> pagamentoService
                            .buscarPagamentoPorCodigoTransacao(
                                    "PIX-INEXISTENTE"
                            )
            );

            assertEquals(
                    "Pagamento não encontrado pela transação",
                    excecao.getMessage()
            );
        }
    }

    @Nested
    class ConfirmacaoManual {

        @Test
        void deveConfirmarPagamentoPendenteManualmente() {
            Pagamento pagamento =
                    novoPagamentoPendente(
                            FormaPagamento.BOLETO,
                            "BOL-123"
                    );

            when(
                    pagamentoRepository.findByIdAndPedidoId(
                            20L,
                            10L
                    )
            ).thenReturn(Optional.of(pagamento));

            configurarPersistenciaRetornandoMesmoPagamento();

            Pagamento resultado =
                    pagamentoService.confirmarPagamentoPendente(
                            10L,
                            20L
                    );

            assertAll(
                    () -> assertSame(
                            pagamento,
                            resultado
                    ),
                    () -> assertEquals(
                            StatusPagamento.APROVADO,
                            resultado.getStatusPagamento()
                    ),
                    () -> assertNotNull(
                            resultado.getCodigoTransacao()
                    ),
                    () -> assertTrue(
                            resultado.getCodigoTransacao()
                                    .startsWith("CONFIRM-")
                    ),
                    () -> assertEquals(
                            "Pagamento pendente confirmado manualmente",
                            resultado.getMensagemRetorno()
                    )
            );

            verify(pagamentoRepository).saveAndFlush(pagamento);
        }

        @Test
        void naoDeveConfirmarManualmentePagamentoAprovado() {
            Pedido pedido = novoPedidoComItem();

            Pagamento pagamento = new Pagamento(
                    pedido,
                    new BigDecimal("200.00"),
                    FormaPagamento.CARTAO_CREDITO
            );

            pagamento.aprovar(
                    "CARD-123",
                    "Cartão aprovado"
            );

            when(
                    pagamentoRepository.findByIdAndPedidoId(
                            20L,
                            10L
                    )
            ).thenReturn(Optional.of(pagamento));

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pagamentoService
                            .confirmarPagamentoPendente(
                                    10L,
                                    20L
                            )
            );

            assertEquals(
                    "Somente pagamento pendente pode ser confirmado",
                    excecao.getMessage()
            );

            verify(
                    pagamentoRepository,
                    never()
            ).saveAndFlush(any());
        }
    }

    @Nested
    class ConfirmacaoPeloGateway {

        @Test
        void deveManterPagamentoQuandoGatewayAindaEstaPendente() {
            Pagamento pagamento =
                    novoPagamentoPendente(
                            FormaPagamento.PIX,
                            "PIX-123"
                    );

            when(
                    pagamentoRepository.findByCodigoTransacao(
                            "PIX-123"
                    )
            ).thenReturn(Optional.of(pagamento));

            when(
                    gatewayPagamentoFakeConsulta.consultarStatus(
                            "PIX-123"
                    )
            ).thenReturn(StatusPagamento.PENDENTE);

            Pagamento resultado =
                    pagamentoService
                            .processarConfirmacaoDoGateway(
                                    "PIX-123"
                            );

            assertAll(
                    () -> assertSame(
                            pagamento,
                            resultado
                    ),
                    () -> assertEquals(
                            StatusPagamento.PENDENTE,
                            resultado.getStatusPagamento()
                    )
            );

            verify(
                    pagamentoRepository,
                    never()
            ).saveAndFlush(any());
        }

        @Test
        void deveAprovarPagamentoPendenteConfirmadoPeloGateway() {
            Pagamento pagamento =
                    novoPagamentoPendente(
                            FormaPagamento.PIX,
                            "PIX-123"
                    );

            when(
                    pagamentoRepository.findByCodigoTransacao(
                            "PIX-123"
                    )
            ).thenReturn(Optional.of(pagamento));

            when(
                    gatewayPagamentoFakeConsulta.consultarStatus(
                            "PIX-123"
                    )
            ).thenReturn(StatusPagamento.APROVADO);

            configurarPersistenciaRetornandoMesmoPagamento();

            Pagamento resultado =
                    pagamentoService
                            .processarConfirmacaoDoGateway(
                                    "PIX-123"
                            );

            assertAll(
                    () -> assertSame(
                            pagamento,
                            resultado
                    ),
                    () -> assertEquals(
                            StatusPagamento.APROVADO,
                            resultado.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            "PIX-123",
                            resultado.getCodigoTransacao()
                    ),
                    () -> assertEquals(
                            "Pagamento confirmado pelo gateway fake via webhook",
                            resultado.getMensagemRetorno()
                    )
            );

            verify(pagamentoRepository).saveAndFlush(pagamento);
        }

        @Test
        void deveRetornarPagamentoJaAprovadoSemProcessarNovamente() {
            Pedido pedido = novoPedidoComItem();

            Pagamento pagamento = new Pagamento(
                    pedido,
                    new BigDecimal("200.00"),
                    FormaPagamento.PIX
            );

            pagamento.aprovar(
                    "PIX-123",
                    "Pagamento já aprovado"
            );

            when(
                    pagamentoRepository.findByCodigoTransacao(
                            "PIX-123"
                    )
            ).thenReturn(Optional.of(pagamento));

            when(
                    gatewayPagamentoFakeConsulta.consultarStatus(
                            "PIX-123"
                    )
            ).thenReturn(StatusPagamento.APROVADO);

            Pagamento resultado =
                    pagamentoService
                            .processarConfirmacaoDoGateway(
                                    "PIX-123"
                            );

            assertSame(
                    pagamento,
                    resultado
            );

            assertEquals(
                    "Pagamento já aprovado",
                    resultado.getMensagemRetorno()
            );

            verify(
                    pagamentoRepository,
                    never()
            ).saveAndFlush(any());
        }

        @Test
        void deveRecusarPagamentoPendenteRecusadoPeloGateway() {
            Pagamento pagamento =
                    novoPagamentoPendente(
                            FormaPagamento.BOLETO,
                            "BOL-123"
                    );

            when(
                    pagamentoRepository.findByCodigoTransacao(
                            "BOL-123"
                    )
            ).thenReturn(Optional.of(pagamento));

            when(
                    gatewayPagamentoFakeConsulta.consultarStatus(
                            "BOL-123"
                    )
            ).thenReturn(StatusPagamento.RECUSADO);

            configurarPersistenciaRetornandoMesmoPagamento();

            Pagamento resultado =
                    pagamentoService
                            .processarConfirmacaoDoGateway(
                                    "BOL-123"
                            );

            assertAll(
                    () -> assertSame(
                            pagamento,
                            resultado
                    ),
                    () -> assertEquals(
                            StatusPagamento.RECUSADO,
                            resultado.getStatusPagamento()
                    ),
                    () -> assertEquals(
                            "BOL-123",
                            resultado.getCodigoTransacao()
                    ),
                    () -> assertEquals(
                            "Pagamento recusado pelo gateway fake via webhook",
                            resultado.getMensagemRetorno()
                    )
            );

            verify(pagamentoRepository).saveAndFlush(pagamento);
        }

        @Test
        void deveRetornarPagamentoJaRecusadoSemProcessarNovamente() {
            Pedido pedido = novoPedidoComItem();

            Pagamento pagamento = new Pagamento(
                    pedido,
                    new BigDecimal("200.00"),
                    FormaPagamento.BOLETO
            );

            pagamento.recusar(
                    "BOL-123",
                    "Pagamento já recusado"
            );

            when(
                    pagamentoRepository.findByCodigoTransacao(
                            "BOL-123"
                    )
            ).thenReturn(Optional.of(pagamento));

            when(
                    gatewayPagamentoFakeConsulta.consultarStatus(
                            "BOL-123"
                    )
            ).thenReturn(StatusPagamento.RECUSADO);

            Pagamento resultado =
                    pagamentoService
                            .processarConfirmacaoDoGateway(
                                    "BOL-123"
                            );

            assertSame(
                    pagamento,
                    resultado
            );

            assertEquals(
                    "Pagamento já recusado",
                    resultado.getMensagemRetorno()
            );

            verify(
                    pagamentoRepository,
                    never()
            ).saveAndFlush(any());
        }

        @Test
        void naoDeveAprovarPagamentoQueNaoEstaPendente() {
            Pedido pedido = novoPedidoComItem();

            Pagamento pagamento = new Pagamento(
                    pedido,
                    new BigDecimal("200.00"),
                    FormaPagamento.CARTAO_CREDITO
            );

            pagamento.recusar(
                    "CARD-123",
                    "Pagamento recusado"
            );

            when(
                    pagamentoRepository.findByCodigoTransacao(
                            "CARD-123"
                    )
            ).thenReturn(Optional.of(pagamento));

            when(
                    gatewayPagamentoFakeConsulta.consultarStatus(
                            "CARD-123"
                    )
            ).thenReturn(StatusPagamento.APROVADO);

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pagamentoService
                            .processarConfirmacaoDoGateway(
                                    "CARD-123"
                            )
            );

            assertEquals(
                    "Somente pagamento pendente pode ser aprovado pelo gateway",
                    excecao.getMessage()
            );

            verify(
                    pagamentoRepository,
                    never()
            ).saveAndFlush(any());
        }

        @Test
        void naoDeveRecusarPagamentoQueNaoEstaPendente() {
            Pedido pedido = novoPedidoComItem();

            Pagamento pagamento = new Pagamento(
                    pedido,
                    new BigDecimal("200.00"),
                    FormaPagamento.CARTAO_CREDITO
            );

            pagamento.aprovar(
                    "CARD-123",
                    "Pagamento aprovado"
            );

            when(
                    pagamentoRepository.findByCodigoTransacao(
                            "CARD-123"
                    )
            ).thenReturn(Optional.of(pagamento));

            when(
                    gatewayPagamentoFakeConsulta.consultarStatus(
                            "CARD-123"
                    )
            ).thenReturn(StatusPagamento.RECUSADO);

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pagamentoService
                            .processarConfirmacaoDoGateway(
                                    "CARD-123"
                            )
            );

            assertEquals(
                    "Somente pagamento pendente pode ser recusado pelo gateway",
                    excecao.getMessage()
            );

            verify(
                    pagamentoRepository,
                    never()
            ).saveAndFlush(any());
        }

        @Test
        void naoDeveProcessarStatusDeGatewayNaoSuportado() {
            Pagamento pagamento =
                    novoPagamentoPendente(
                            FormaPagamento.PIX,
                            "PIX-123"
                    );

            when(
                    pagamentoRepository.findByCodigoTransacao(
                            "PIX-123"
                    )
            ).thenReturn(Optional.of(pagamento));

            when(
                    gatewayPagamentoFakeConsulta.consultarStatus(
                            "PIX-123"
                    )
            ).thenReturn(StatusPagamento.CANCELADO);

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    () -> pagamentoService
                            .processarConfirmacaoDoGateway(
                                    "PIX-123"
                            )
            );

            assertEquals(
                    "Status de gateway não suportado: CANCELADO",
                    excecao.getMessage()
            );

            verify(
                    pagamentoRepository,
                    never()
            ).saveAndFlush(any());
        }

        @Test
        void naoDeveProcessarTransacaoInexistente() {
            when(
                    pagamentoRepository.findByCodigoTransacao(
                            "PIX-INEXISTENTE"
                    )
            ).thenReturn(Optional.empty());

            RuntimeException excecao = assertThrows(
                    RuntimeException.class,
                    () -> pagamentoService
                            .processarConfirmacaoDoGateway(
                                    "PIX-INEXISTENTE"
                            )
            );

            assertEquals(
                    "Pagamento não encontrado pela transação",
                    excecao.getMessage()
            );

            verifyNoInteractions(
                    gatewayPagamentoFakeConsulta
            );
        }
    }

    private Pedido novoPedidoVazio() {
        Usuario usuario = new Usuario(
                "Filipe",
                "filipe@teste.com",
                "123456",
                Perfil.USER
        );

        return new Pedido(usuario);
    }

    private Pedido novoPedidoComItem() {
        Pedido pedido = novoPedidoVazio();

        Produto produto = new Produto(
                "Mouse",
                "Mouse sem fio",
                new BigDecimal("100.00"),
                20
        );

        pedido.adicionarItem(
                produto,
                2
        );

        return pedido;
    }

    private Pagamento novoPagamentoPendente(
            FormaPagamento formaPagamento,
            String codigoTransacao) {
        Pedido pedido = novoPedidoComItem();

        Pagamento pagamento = new Pagamento(
                pedido,
                pedido.getValorFinal(),
                formaPagamento
        );

        pagamento.deixarPendente(
                codigoTransacao,
                "Pagamento aguardando confirmação"
        );

        return pagamento;
    }

    private void configurarPersistenciaRetornandoMesmoPagamento() {
        when(
                pagamentoRepository.saveAndFlush(
                        any(Pagamento.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );
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

    private void assertBigDecimalEquals(
            String esperado,
            BigDecimal atual) {
        assertEquals(
                0,
                new BigDecimal(esperado).compareTo(atual)
        );
    }
}