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

    private FakePagamentoWebhookDTO novoDto() {
        return new FakePagamentoWebhookDTO(
                "evt-1",
                "PAYMENT_UPDATED",
                "PIX-123",
                StatusPagamento.APROVADO,
                "2026-07-13T10:00:00"
        );
    }
}
