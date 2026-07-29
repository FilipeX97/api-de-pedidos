package br.com.api.pedidos.payment.webhook.service;

import br.com.api.pedidos.payment.adapter.fake
        .GatewayPagamentoFakeConsulta;
import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.facade.CheckoutFacade;
import br.com.api.pedidos.payment.webhook.document.entity
        .RegistroOperacionalWebhookPagamento;
import br.com.api.pedidos.payment.webhook.document.service
        .RegistroOperacionalWebhookPagamentoService;
import br.com.api.pedidos.payment.webhook.dto
        .FakePagamentoWebhookDTO;
import br.com.api.pedidos.payment.webhook.entity
        .WebhookPagamentoRecebido;
import br.com.api.pedidos.payment.webhook.service.result
        .ResultadoRegistroWebhook;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FakePagamentoWebhookServiceTest {

    @Mock
    private AssinaturaWebhookFakeService
            assinaturaWebhookFakeService;

    @Mock
    private GatewayPagamentoFakeConsulta
            gatewayPagamentoFakeConsulta;

    @Mock
    private CheckoutFacade checkoutFacade;

    @Mock
    private WebhookPagamentoRecebidoService
            webhookPagamentoRecebidoService;

    @Mock
    private RegistroOperacionalWebhookPagamentoService
            registroOperacionalWebhookPagamentoService;

    private FakePagamentoWebhookService service;

    @BeforeEach
    void setUp() {
        service = new FakePagamentoWebhookService(
                new ObjectMapper(),
                assinaturaWebhookFakeService,
                gatewayPagamentoFakeConsulta,
                checkoutFacade,
                webhookPagamentoRecebidoService,
                registroOperacionalWebhookPagamentoService
        );
    }

    @Test
    void deveProcessarWebhookNovo() {
        String payload = payloadValido();

        WebhookPagamentoRecebido evento =
                criarEventoTransacional(payload);

        RegistroOperacionalWebhookPagamento registroOperacional =
                criarRegistroOperacional(payload);

        PagamentoResponseDTO resposta =
                respostaPagamento(
                        StatusPagamento.APROVADO
                );

        when(registroOperacionalWebhookPagamentoService
                .registrarRecebimento(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                ))
                .thenReturn(
                        Optional.of(registroOperacional)
                );

        when(webhookPagamentoRecebidoService
                .registrarOuBuscarExistente(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                ))
                .thenReturn(
                        new ResultadoRegistroWebhook(
                                evento,
                                true
                        )
                );

        when(checkoutFacade
                .processarWebhookPagamento("PIX-123"))
                .thenReturn(resposta);

        PagamentoResponseDTO resultado =
                service.processarWebhook(
                        payload,
                        "assinatura"
                );

        assertEquals(
                StatusPagamento.APROVADO,
                resultado.statusPagamento()
        );

        verify(gatewayPagamentoFakeConsulta)
                .simularAtualizacaoExterna(
                        "PIX-123",
                        StatusPagamento.APROVADO
                );

        verify(checkoutFacade)
                .processarWebhookPagamento("PIX-123");

        verify(webhookPagamentoRecebidoService)
                .marcarComoProcessado(evento);

        verify(registroOperacionalWebhookPagamentoService)
                .marcarComoProcessado(
                        registroOperacional
                );

        verify(registroOperacionalWebhookPagamentoService, never())
                .sinalizarDuplicidade(any());

        verify(registroOperacionalWebhookPagamentoService, never())
                .marcarComoDuplicado(any());

        verify(registroOperacionalWebhookPagamentoService, never())
                .marcarComoErro(any(), any());
    }

    @Test
    void naoDeveProcessarNovamenteWebhookDuplicadoJaProcessado() {
        String payload = payloadValido();

        WebhookPagamentoRecebido evento =
                criarEventoTransacional(payload);

        evento.marcarComoProcessado();

        RegistroOperacionalWebhookPagamento registroOperacional =
                criarRegistroOperacional(payload);

        PagamentoResponseDTO resposta =
                respostaPagamento(
                        StatusPagamento.APROVADO
                );

        when(registroOperacionalWebhookPagamentoService
                .registrarRecebimento(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                ))
                .thenReturn(
                        Optional.of(registroOperacional)
                );

        when(webhookPagamentoRecebidoService
                .registrarOuBuscarExistente(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                ))
                .thenReturn(
                        new ResultadoRegistroWebhook(
                                evento,
                                false
                        )
                );

        when(checkoutFacade
                .buscarPagamentoPorCodigoTransacao(
                        "PIX-123"
                ))
                .thenReturn(resposta);

        PagamentoResponseDTO resultado =
                service.processarWebhook(
                        payload,
                        "assinatura"
                );

        assertEquals(
                StatusPagamento.APROVADO,
                resultado.statusPagamento()
        );

        verify(gatewayPagamentoFakeConsulta, never())
                .simularAtualizacaoExterna(
                        anyString(),
                        any()
                );

        verify(checkoutFacade, never())
                .processarWebhookPagamento(anyString());

        verify(checkoutFacade)
                .buscarPagamentoPorCodigoTransacao(
                        "PIX-123"
                );

        verify(registroOperacionalWebhookPagamentoService)
                .sinalizarDuplicidade(
                        registroOperacional
                );

        verify(registroOperacionalWebhookPagamentoService)
                .marcarComoDuplicado(
                        registroOperacional
                );

        verify(registroOperacionalWebhookPagamentoService, never())
                .marcarComoProcessado(any());

        verify(registroOperacionalWebhookPagamentoService, never())
                .marcarComoErro(any(), any());

        verify(webhookPagamentoRecebidoService, never())
                .marcarComoProcessado(any());

        verify(webhookPagamentoRecebidoService, never())
                .marcarComoErro(any(), any());
    }

    @Test
    void naoDeveRegistrarOperacionalComAssinaturaInvalida() {
        String payload = payloadValido();

        doThrow(
                new SecurityException(
                        "Assinatura inválida"
                )
        )
                .when(assinaturaWebhookFakeService)
                .validarAssinatura(
                        payload,
                        "assinatura-invalida"
                );

        assertThrows(
                SecurityException.class,
                () -> service.processarWebhook(
                        payload,
                        "assinatura-invalida"
                )
        );

        verifyNoInteractions(
                registroOperacionalWebhookPagamentoService
        );

        verifyNoInteractions(
                webhookPagamentoRecebidoService
        );

        verifyNoInteractions(
                gatewayPagamentoFakeConsulta
        );

        verifyNoInteractions(checkoutFacade);
    }

    @Test
    void deveReprocessarWebhookDuplicadoComErro() {
        String payload = payloadValido();

        WebhookPagamentoRecebido evento =
                criarEventoTransacional(payload);

        evento.marcarComoErro(
                "Erro anterior"
        );

        RegistroOperacionalWebhookPagamento registroOperacional =
                criarRegistroOperacional(payload);

        PagamentoResponseDTO resposta =
                respostaPagamento(
                        StatusPagamento.APROVADO
                );

        when(registroOperacionalWebhookPagamentoService
                .registrarRecebimento(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                ))
                .thenReturn(
                        Optional.of(registroOperacional)
                );

        when(webhookPagamentoRecebidoService
                .registrarOuBuscarExistente(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                ))
                .thenReturn(
                        new ResultadoRegistroWebhook(
                                evento,
                                false
                        )
                );

        when(checkoutFacade
                .processarWebhookPagamento("PIX-123"))
                .thenReturn(resposta);

        PagamentoResponseDTO resultado =
                service.processarWebhook(
                        payload,
                        "assinatura"
                );

        assertEquals(
                StatusPagamento.APROVADO,
                resultado.statusPagamento()
        );

        verify(gatewayPagamentoFakeConsulta)
                .simularAtualizacaoExterna(
                        "PIX-123",
                        StatusPagamento.APROVADO
                );

        verify(checkoutFacade)
                .processarWebhookPagamento("PIX-123");

        verify(webhookPagamentoRecebidoService)
                .marcarComoProcessado(evento);

        verify(registroOperacionalWebhookPagamentoService)
                .sinalizarDuplicidade(
                        registroOperacional
                );

        verify(registroOperacionalWebhookPagamentoService)
                .marcarComoProcessado(
                        registroOperacional
                );

        verify(registroOperacionalWebhookPagamentoService, never())
                .marcarComoDuplicado(any());

        verify(registroOperacionalWebhookPagamentoService, never())
                .marcarComoErro(any(), any());
    }

    @Test
    void deveMarcarRegistrosComoErroQuandoProcessamentoFalhar() {
        String payload = payloadValido();

        WebhookPagamentoRecebido evento =
                criarEventoTransacional(payload);

        RegistroOperacionalWebhookPagamento registroOperacional =
                criarRegistroOperacional(payload);

        IllegalStateException exception =
                new IllegalStateException(
                        "Falha ao processar pagamento"
                );

        when(registroOperacionalWebhookPagamentoService
                .registrarRecebimento(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                ))
                .thenReturn(
                        Optional.of(registroOperacional)
                );

        when(webhookPagamentoRecebidoService
                .registrarOuBuscarExistente(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                ))
                .thenReturn(
                        new ResultadoRegistroWebhook(
                                evento,
                                true
                        )
                );

        when(checkoutFacade
                .processarWebhookPagamento("PIX-123"))
                .thenThrow(exception);

        IllegalStateException exceptionLancada =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.processarWebhook(
                                payload,
                                "assinatura"
                        )
                );

        assertSame(
                exception,
                exceptionLancada
        );

        verify(gatewayPagamentoFakeConsulta)
                .simularAtualizacaoExterna(
                        "PIX-123",
                        StatusPagamento.APROVADO
                );

        verify(checkoutFacade)
                .processarWebhookPagamento("PIX-123");

        verify(webhookPagamentoRecebidoService, never())
                .marcarComoProcessado(any());

        verify(webhookPagamentoRecebidoService)
                .marcarComoErro(
                        evento,
                        exception
                );

        verify(registroOperacionalWebhookPagamentoService)
                .marcarComoErro(
                        registroOperacional,
                        exception
                );

        verify(registroOperacionalWebhookPagamentoService, never())
                .marcarComoProcessado(any());

        verify(registroOperacionalWebhookPagamentoService, never())
                .marcarComoDuplicado(any());
    }

    @Test
    void deveContinuarQuandoRegistroOperacionalNaoForCriado() {
        String payload = payloadValido();

        WebhookPagamentoRecebido evento =
                criarEventoTransacional(payload);

        PagamentoResponseDTO resposta =
                respostaPagamento(
                        StatusPagamento.APROVADO
                );

        when(registroOperacionalWebhookPagamentoService
                .registrarRecebimento(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                ))
                .thenReturn(Optional.empty());

        when(webhookPagamentoRecebidoService
                .registrarOuBuscarExistente(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                ))
                .thenReturn(
                        new ResultadoRegistroWebhook(
                                evento,
                                true
                        )
                );

        when(checkoutFacade
                .processarWebhookPagamento("PIX-123"))
                .thenReturn(resposta);

        PagamentoResponseDTO resultado =
                service.processarWebhook(
                        payload,
                        "assinatura"
                );

        assertEquals(
                StatusPagamento.APROVADO,
                resultado.statusPagamento()
        );

        verify(gatewayPagamentoFakeConsulta)
                .simularAtualizacaoExterna(
                        "PIX-123",
                        StatusPagamento.APROVADO
                );

        verify(checkoutFacade)
                .processarWebhookPagamento("PIX-123");

        verify(webhookPagamentoRecebidoService)
                .marcarComoProcessado(evento);

        verify(registroOperacionalWebhookPagamentoService)
                .registrarRecebimento(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                );

        verifyNoMoreInteractions(
                registroOperacionalWebhookPagamentoService
        );
    }

    @Test
    void deveMarcarOperacionalComoErroQuandoPostgreSqlFalhar() {
        String payload = payloadValido();

        RegistroOperacionalWebhookPagamento registroOperacional =
                criarRegistroOperacional(payload);

        IllegalStateException exception =
                new IllegalStateException(
                        "Falha ao registrar webhook no PostgreSQL"
                );

        when(registroOperacionalWebhookPagamentoService
                .registrarRecebimento(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                ))
                .thenReturn(
                        Optional.of(registroOperacional)
                );

        when(webhookPagamentoRecebidoService
                .registrarOuBuscarExistente(
                        any(FakePagamentoWebhookDTO.class),
                        eq(payload)
                ))
                .thenThrow(exception);

        IllegalStateException exceptionLancada =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.processarWebhook(
                                payload,
                                "assinatura"
                        )
                );

        assertSame(
                exception,
                exceptionLancada
        );

        verify(registroOperacionalWebhookPagamentoService)
                .marcarComoErro(
                        registroOperacional,
                        exception
                );

        verifyNoInteractions(
                gatewayPagamentoFakeConsulta
        );

        verifyNoInteractions(checkoutFacade);
    }

    private WebhookPagamentoRecebido
    criarEventoTransacional(
            String payload
    ) {
        return new WebhookPagamentoRecebido(
                "evt-1",
                "PIX-123",
                StatusPagamento.APROVADO,
                payload
        );
    }

    private RegistroOperacionalWebhookPagamento
    criarRegistroOperacional(
            String payload
    ) {
        return new RegistroOperacionalWebhookPagamento(
                "evt-1",
                "PIX-123",
                StatusPagamento.APROVADO,
                payload,
                "request-123",
                "PAYMENT_UPDATED",
                "FAKE_GATEWAY"
        );
    }

    private String payloadValido() {
        return """
                {
                  "eventId": "evt-1",
                  "tipo": "PAYMENT_UPDATED",
                  "codigoTransacao": "PIX-123",
                  "statusPagamento": "APROVADO",
                  "dataEvento": "2026-07-13T10:00:00"
                }
                """;
    }

    private PagamentoResponseDTO respostaPagamento(
            StatusPagamento statusPagamento
    ) {
        return new PagamentoResponseDTO(
                1L,
                10L,
                BigDecimal.valueOf(100),
                FormaPagamento.PIX,
                statusPagamento,
                "PIX-123",
                "Pagamento confirmado",
                LocalDateTime.now()
        );
    }
}