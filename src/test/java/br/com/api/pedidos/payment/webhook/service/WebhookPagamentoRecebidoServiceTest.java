package br.com.api.pedidos.payment.webhook.service;

import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.webhook.dto.FakePagamentoWebhookDTO;
import br.com.api.pedidos.payment.webhook.entity.WebhookPagamentoRecebido;
import br.com.api.pedidos.payment.webhook.repository.WebhookPagamentoRecebidoRepository;
import br.com.api.pedidos.payment.webhook.service.result.ResultadoRegistroWebhook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookPagamentoRecebidoServiceTest {

    @Mock
    private WebhookPagamentoRecebidoRepository repository;

    @InjectMocks
    private WebhookPagamentoRecebidoService service;

    @Test
    void deveRegistrarWebhookNovo() {
        FakePagamentoWebhookDTO dto = novoDto();
        String payload = "{}";

        when(repository.findByEventId("evt-1"))
                .thenReturn(Optional.empty());

        when(repository.saveAndFlush(any(WebhookPagamentoRecebido.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResultadoRegistroWebhook resultado =
                service.registrarOuBuscarExistente(dto, payload);

        assertTrue(resultado.novo());
        assertFalse(resultado.duplicado());
        assertTrue(resultado.evento().estaRecebido());
        assertFalse(resultado.evento().estaProcessado());
        assertFalse(resultado.evento().estaComErro());
        assertEquals("evt-1", resultado.evento().getEventId());

        verify(repository).saveAndFlush(any(WebhookPagamentoRecebido.class));
    }

    @Test
    void deveRetornarEventoExistenteQuandoJaFoiRecebido() {
        FakePagamentoWebhookDTO dto = novoDto();
        String payload = "{}";

        WebhookPagamentoRecebido existente =
                new WebhookPagamentoRecebido(
                        "evt-1",
                        "PIX-123",
                        StatusPagamento.APROVADO,
                        payload
                );

        when(repository.findByEventId("evt-1"))
                .thenReturn(Optional.of(existente));

        ResultadoRegistroWebhook resultado =
                service.registrarOuBuscarExistente(dto, payload);

        assertFalse(resultado.novo());
        assertTrue(resultado.duplicado());
        assertEquals("PIX-123", resultado.evento().getCodigoTransacao());

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deveTratarDuplicidadeConcorrenteComDataIntegrityViolationException() {
        FakePagamentoWebhookDTO dto = novoDto();
        String payload = "{}";

        WebhookPagamentoRecebido existente =
                new WebhookPagamentoRecebido(
                        "evt-1",
                        "PIX-123",
                        StatusPagamento.APROVADO,
                        payload
                );

        when(repository.findByEventId("evt-1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existente));

        when(repository.saveAndFlush(any(WebhookPagamentoRecebido.class)))
                .thenThrow(new DataIntegrityViolationException("event_id duplicado"));

        ResultadoRegistroWebhook resultado =
                service.registrarOuBuscarExistente(dto, payload);

        assertFalse(resultado.novo());
        assertTrue(resultado.duplicado());
        assertEquals("PIX-123", resultado.evento().getCodigoTransacao());
    }

    @Test
    void deveMarcarWebhookComoProcessado() {
        WebhookPagamentoRecebido webhook =
                new WebhookPagamentoRecebido(
                        "evt-1",
                        "PIX-123",
                        StatusPagamento.APROVADO,
                        "{}"
                );

        when(repository.saveAndFlush(webhook))
                .thenReturn(webhook);

        WebhookPagamentoRecebido resultado =
                service.marcarComoProcessado(webhook);

        assertTrue(resultado.estaProcessado());
        assertFalse(resultado.estaComErro());
        assertNull(resultado.getMensagemErro());
        assertNotNull(resultado.getDataProcessamento());

        verify(repository).saveAndFlush(webhook);
    }

    @Test
    void deveMarcarWebhookComoErro() {
        WebhookPagamentoRecebido webhook =
                new WebhookPagamentoRecebido(
                        "evt-1",
                        "PIX-123",
                        StatusPagamento.APROVADO,
                        "{}"
                );

        IllegalStateException exception =
                new IllegalStateException("Falha ao processar pagamento");

        when(repository.saveAndFlush(webhook))
                .thenReturn(webhook);

        WebhookPagamentoRecebido resultado =
                service.marcarComoErro(webhook, exception);

        assertTrue(resultado.estaComErro());
        assertFalse(resultado.estaProcessado());
        assertEquals(
                "Falha ao processar pagamento",
                resultado.getMensagemErro()
        );
        assertNotNull(resultado.getDataProcessamento());

        verify(repository).saveAndFlush(webhook);
    }

    private FakePagamentoWebhookDTO novoDto() {
        return new FakePagamentoWebhookDTO(
                "evt-1",
                "PAYMENT_UPDATED",
                "PIX-123",
                StatusPagamento.APROVADO,
                LocalDateTime.of(2026, 7, 13, 10, 0, 0)
        );
    }
}
