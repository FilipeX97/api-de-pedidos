package br.com.api.pedidos.payment.webhook.document.service;

import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.webhook.document.entity.RegistroOperacionalWebhookPagamento;
import br.com.api.pedidos.payment.webhook.document.repository.RegistroOperacionalWebhookPagamentoRepository;
import br.com.api.pedidos.payment.webhook.dto.FakePagamentoWebhookDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroOperacionalWebhookPagamentoServiceTest {

    @Mock
    private RegistroOperacionalWebhookPagamentoRepository repository;

    @InjectMocks
    private RegistroOperacionalWebhookPagamentoService service;

    @AfterEach
    void limparMdc() {
        MDC.clear();
    }

    @Test
    void deveCriarRegistroOperacionalRecebido() {
        MDC.put("requestId", "request-test-123");

        FakePagamentoWebhookDTO dto = novoDto();
        String payload = payloadValido();

        when(repository.save(
                any(RegistroOperacionalWebhookPagamento.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        Optional<RegistroOperacionalWebhookPagamento> resultado =
                service.registrarRecebimento(
                        dto,
                        payload
                );

        assertTrue(resultado.isPresent());

        RegistroOperacionalWebhookPagamento registro =
                resultado.orElseThrow();

        assertEquals("evt-123", registro.getEventId());
        assertEquals(
                "PIX-123",
                registro.getCodigoTransacao()
        );
        assertEquals(
                StatusPagamento.APROVADO,
                registro.getStatusRecebido()
        );
        assertEquals(
                "request-test-123",
                registro.getRequestId()
        );
        assertEquals(
                "PAYMENT_UPDATED",
                registro.getTipoEvento()
        );
        assertEquals(
                "FAKE_GATEWAY",
                registro.getOrigem()
        );
        assertTrue(registro.estaRecebido());

        ArgumentCaptor<RegistroOperacionalWebhookPagamento>
                captor =
                ArgumentCaptor.forClass(
                        RegistroOperacionalWebhookPagamento.class
                );

        verify(repository).save(captor.capture());

        assertEquals(
                payload,
                captor.getValue().getPayloadOriginal()
        );
    }

    @Test
    void devePermitirRequestIdAusente() {
        when(repository.save(
                any(RegistroOperacionalWebhookPagamento.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        Optional<RegistroOperacionalWebhookPagamento> resultado =
                service.registrarRecebimento(
                        novoDto(),
                        payloadValido()
                );

        assertTrue(resultado.isPresent());
        assertEquals(
                null,
                resultado.orElseThrow().getRequestId()
        );
    }

    @Test
    void deveRetornarOptionalVazioQuandoMongoFalharAoCriar() {
        when(repository.save(
                any(RegistroOperacionalWebhookPagamento.class)
        )).thenThrow(
                new DataAccessResourceFailureException(
                        "MongoDB indisponível"
                )
        );

        Optional<RegistroOperacionalWebhookPagamento> resultado =
                assertDoesNotThrow(
                        () -> service.registrarRecebimento(
                                novoDto(),
                                payloadValido()
                        )
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void naoDeveTentarSalvarQuandoDtoForNulo() {
        Optional<RegistroOperacionalWebhookPagamento> resultado =
                assertDoesNotThrow(
                        () -> service.registrarRecebimento(
                                null,
                                payloadValido()
                        )
                );

        assertTrue(resultado.isEmpty());

        verify(repository, never()).save(any());
    }

    @Test
    void deveMarcarComoProcessado() {
        RegistroOperacionalWebhookPagamento registro =
                novoRegistro();

        service.marcarComoProcessado(registro);

        assertTrue(registro.estaProcessado());
        assertNotNull(registro.getDataProcessamento());
        assertNotNull(registro.getDuracaoProcessamentoMs());

        verify(repository).save(registro);
    }

    @Test
    void deveMarcarComoDuplicado() {
        RegistroOperacionalWebhookPagamento registro =
                novoRegistro();

        service.marcarComoDuplicado(registro);

        assertTrue(registro.estaDuplicado());
        assertTrue(registro.foiIgnoradoComoDuplicado());

        verify(repository).save(registro);
    }

    @Test
    void deveSinalizarDuplicidadeSemFinalizarProcessamento() {
        RegistroOperacionalWebhookPagamento registro =
                novoRegistro();

        service.sinalizarDuplicidade(registro);

        assertTrue(registro.estaDuplicado());
        assertTrue(registro.estaRecebido());
        assertFalse(registro.estaFinalizado());

        verify(repository).save(registro);
    }

    @Test
    void deveMarcarComoErro() {
        RegistroOperacionalWebhookPagamento registro =
                novoRegistro();

        IllegalStateException exception =
                new IllegalStateException(
                        "Falha ao processar pagamento"
                );

        service.marcarComoErro(
                registro,
                exception
        );

        assertTrue(registro.estaComErro());
        assertEquals(
                "Falha ao processar pagamento",
                registro.getMensagemErro()
        );

        verify(repository).save(registro);
    }

    @Test
    void falhaAoAtualizarMongoNaoDeveSerRelancada() {
        RegistroOperacionalWebhookPagamento registro =
                novoRegistro();

        doThrow(
                new DataAccessResourceFailureException(
                        "MongoDB indisponível"
                )
        )
                .when(repository)
                .save(registro);

        assertDoesNotThrow(
                () -> service.marcarComoProcessado(registro)
        );

        /*
         * A alteração aconteceu no objeto em memória, mas não foi
         * confirmada no MongoDB. O importante é que a exceção não
         * interrompa o fluxo principal.
         */
        assertTrue(registro.estaProcessado());
    }

    @Test
    void registroNuloNaoDeveInterromperFluxo() {
        assertDoesNotThrow(
                () -> service.marcarComoProcessado(null)
        );

        verify(repository, never()).save(any());
    }

    private FakePagamentoWebhookDTO novoDto() {
        return new FakePagamentoWebhookDTO(
                "evt-123",
                "PAYMENT_UPDATED",
                "PIX-123",
                StatusPagamento.APROVADO,
                LocalDateTime.
                        of(
                        2026,
                        7,
                        28,
                        20,
                        30
                )
        );
    }

    private RegistroOperacionalWebhookPagamento novoRegistro() {
        return new RegistroOperacionalWebhookPagamento(
                "evt-123",
                "PIX-123",
                StatusPagamento.APROVADO,
                payloadValido(),
                "request-123",
                "PAYMENT_UPDATED",
                "FAKE_GATEWAY"
        );
    }

    private String payloadValido() {
        return """
                {
                  "eventId": "evt-123",
                  "tipo": "PAYMENT_UPDATED",
                  "codigoTransacao": "PIX-123",
                  "statusPagamento": "APROVADO"
                }
                """;
    }
}