package br.com.api.pedidos.payment.webhook.service;

import br.com.api.pedidos.payment.adapter.fake.GatewayPagamentoFakeConsulta;
import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.facade.CheckoutFacade;
import br.com.api.pedidos.payment.webhook.entity.WebhookPagamentoRecebido;
import br.com.api.pedidos.payment.webhook.service.result.ResultadoRegistroWebhook;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FakePagamentoWebhookServiceTest {

    @Mock
    private AssinaturaWebhookFakeService assinaturaWebhookFakeService;

    @Mock
    private GatewayPagamentoFakeConsulta gatewayPagamentoFakeConsulta;

    @Mock
    private CheckoutFacade checkoutFacade;

    @Mock
    private WebhookPagamentoRecebidoService webhookPagamentoRecebidoService;

    private FakePagamentoWebhookService service;

    @BeforeEach
    void setUp() {
        service = new FakePagamentoWebhookService(
                new ObjectMapper(),
                assinaturaWebhookFakeService,
                gatewayPagamentoFakeConsulta,
                checkoutFacade,
                webhookPagamentoRecebidoService
        );
    }

    @Test
    void deveProcessarWebhookNovo() {
        String payload = payloadValido();

        WebhookPagamentoRecebido evento =
                new WebhookPagamentoRecebido(
                        "evt-1",
                        "PIX-123",
                        StatusPagamento.APROVADO,
                        payload
                );

        PagamentoResponseDTO resposta =
                respostaPagamento(StatusPagamento.APROVADO);

        when(webhookPagamentoRecebidoService.registrarOuBuscarExistente(
                any(),
                eq(payload)
        )).thenReturn(new ResultadoRegistroWebhook(evento, true));

        when(checkoutFacade.processarWebhookPagamento("PIX-123"))
                .thenReturn(resposta);

        PagamentoResponseDTO resultado =
                service.processarWebhook(payload, "assinatura");

        assertEquals(StatusPagamento.APROVADO, resultado.statusPagamento());

        verify(gatewayPagamentoFakeConsulta).simularAtualizacaoExterna(
                "PIX-123",
                StatusPagamento.APROVADO
        );

        verify(checkoutFacade).processarWebhookPagamento("PIX-123");

        verify(webhookPagamentoRecebidoService)
                .marcarComoProcessado(evento);

        verify(webhookPagamentoRecebidoService, never())
                .marcarComoErro(any(), any());
    }

    @Test
    void naoDeveProcessarNovamenteWebhookDuplicadoJaProcessado() {
        String payload = payloadValido();

        WebhookPagamentoRecebido evento =
                new WebhookPagamentoRecebido(
                        "evt-1",
                        "PIX-123",
                        StatusPagamento.APROVADO,
                        payload
                );

        evento.marcarComoProcessado();

        PagamentoResponseDTO resposta =
                respostaPagamento(StatusPagamento.APROVADO);

        when(webhookPagamentoRecebidoService.registrarOuBuscarExistente(
                any(),
                eq(payload)
        )).thenReturn(new ResultadoRegistroWebhook(evento, false));

        when(checkoutFacade.buscarPagamentoPorCodigoTransacao("PIX-123"))
                .thenReturn(resposta);

        PagamentoResponseDTO resultado =
                service.processarWebhook(payload, "assinatura");

        assertEquals(StatusPagamento.APROVADO, resultado.statusPagamento());

        verify(gatewayPagamentoFakeConsulta, never())
                .simularAtualizacaoExterna(anyString(), any());

        verify(checkoutFacade, never())
                .processarWebhookPagamento(anyString());

        verify(checkoutFacade)
                .buscarPagamentoPorCodigoTransacao("PIX-123");

        verify(webhookPagamentoRecebidoService, never())
                .marcarComoProcessado(any());

        verify(webhookPagamentoRecebidoService, never())
                .marcarComoErro(any(), any());
    }

    @Test
    void naoDeveProcessarWebhookComAssinaturaInvalida() {
        String payload = payloadValido();

        doThrow(new SecurityException("Assinatura inválida"))
                .when(assinaturaWebhookFakeService)
                .validarAssinatura(payload, "assinatura-invalida");

        assertThrows(
                SecurityException.class,
                () -> service.processarWebhook(
                        payload,
                        "assinatura-invalida"
                )
        );

        verifyNoInteractions(webhookPagamentoRecebidoService);
        verifyNoInteractions(gatewayPagamentoFakeConsulta);
        verifyNoInteractions(checkoutFacade);
    }

    @Test
    void deveReprocessarWebhookDuplicadoComErro() {
        String payload = payloadValido();

        WebhookPagamentoRecebido evento =
                new WebhookPagamentoRecebido(
                        "evt-1",
                        "PIX-123",
                        StatusPagamento.APROVADO,
                        payload
                );

        evento.marcarComoErro("Erro anterior");

        PagamentoResponseDTO resposta =
                respostaPagamento(StatusPagamento.APROVADO);

        when(webhookPagamentoRecebidoService.registrarOuBuscarExistente(
                any(),
                eq(payload)
        )).thenReturn(new ResultadoRegistroWebhook(evento, false));

        when(checkoutFacade.processarWebhookPagamento("PIX-123"))
                .thenReturn(resposta);

        PagamentoResponseDTO resultado =
                service.processarWebhook(payload, "assinatura");

        assertEquals(StatusPagamento.APROVADO, resultado.statusPagamento());

        verify(gatewayPagamentoFakeConsulta)
                .simularAtualizacaoExterna(
                        "PIX-123",
                        StatusPagamento.APROVADO
                );

        verify(checkoutFacade)
                .processarWebhookPagamento("PIX-123");

        verify(webhookPagamentoRecebidoService)
                .marcarComoProcessado(evento);

        verify(webhookPagamentoRecebidoService, never())
                .marcarComoErro(any(), any());
    }

    @Test
    void deveMarcarWebhookComoErroQuandoProcessamentoFalhar() {
        String payload = payloadValido();

        WebhookPagamentoRecebido evento =
                new WebhookPagamentoRecebido(
                        "evt-1",
                        "PIX-123",
                        StatusPagamento.APROVADO,
                        payload
                );

        IllegalStateException exception =
                new IllegalStateException("Falha ao processar pagamento");

        when(webhookPagamentoRecebidoService.registrarOuBuscarExistente(
                any(),
                eq(payload)
        )).thenReturn(new ResultadoRegistroWebhook(evento, true));

        when(checkoutFacade.processarWebhookPagamento("PIX-123"))
                .thenThrow(exception);

        assertThrows(
                IllegalStateException.class,
                () -> service.processarWebhook(payload, "assinatura")
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
                .marcarComoErro(evento, exception);
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
