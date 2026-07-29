package br.com.api.pedidos.payment.webhook.document.entity;

import br.com.api.pedidos.payment.entity.StatusPagamento;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroOperacionalWebhookPagamentoTest {

    @Test
    void deveCriarRegistroComoRecebido() {
        RegistroOperacionalWebhookPagamento registro =
                novoRegistro();

        assertAll(
                () -> assertNull(registro.getId()),
                () -> assertEquals(
                        "evt-123",
                        registro.getEventId()
                ),
                () -> assertEquals(
                        "PIX-123",
                        registro.getCodigoTransacao()
                ),
                () -> assertEquals(
                        StatusPagamento.APROVADO,
                        registro.getStatusRecebido()
                ),
                () -> assertEquals(
                        StatusRegistroOperacionalWebhook.RECEBIDO,
                        registro.getStatusProcessamento()
                ),
                () -> assertEquals(
                        "request-123",
                        registro.getRequestId()
                ),
                () -> assertEquals(
                        "PAYMENT_UPDATED",
                        registro.getTipoEvento()
                ),
                () -> assertEquals(
                        "FAKE_GATEWAY",
                        registro.getOrigem()
                ),
                () -> assertNotNull(
                        registro.getDataRecebimento()
                ),
                () -> assertNull(
                        registro.getDataProcessamento()
                ),
                () -> assertNull(
                        registro.getDuracaoProcessamentoMs()
                ),
                () -> assertNull(
                        registro.getMensagemErro()
                ),
                () -> assertFalse(registro.estaDuplicado()),
                () -> assertFalse(registro.estaFinalizado()),
                () -> assertTrue(registro.estaRecebido())
        );
    }

    @Test
    void deveMarcarComoProcessado() {
        RegistroOperacionalWebhookPagamento registro =
                novoRegistro();

        registro.marcarComoProcessado();

        assertAll(
                () -> assertEquals(
                        StatusRegistroOperacionalWebhook.PROCESSADO,
                        registro.getStatusProcessamento()
                ),
                () -> assertTrue(registro.estaProcessado()),
                () -> assertTrue(registro.estaFinalizado()),
                () -> assertFalse(registro.estaComErro()),
                () -> assertFalse(registro.estaDuplicado()),
                () -> assertNotNull(
                        registro.getDataProcessamento()
                ),
                () -> assertNotNull(
                        registro.getDuracaoProcessamentoMs()
                ),
                () -> assertTrue(
                        registro.getDuracaoProcessamentoMs() >= 0
                ),
                () -> assertNull(registro.getMensagemErro())
        );
    }

    @Test
    void devePreservarDuplicidadeQuandoReprocessarComSucesso() {
        RegistroOperacionalWebhookPagamento registro =
                novoRegistro();

        registro.sinalizarDuplicidade();
        registro.marcarComoProcessado();

        assertAll(
                () -> assertTrue(registro.estaDuplicado()),
                () -> assertTrue(registro.isDuplicado()),
                () -> assertTrue(registro.estaProcessado()),
                () -> assertFalse(
                        registro.foiIgnoradoComoDuplicado()
                ),
                () -> assertEquals(
                        StatusRegistroOperacionalWebhook.PROCESSADO,
                        registro.getStatusProcessamento()
                )
        );
    }

    @Test
    void deveMarcarComoDuplicadoIgnorado() {
        RegistroOperacionalWebhookPagamento registro =
                novoRegistro();

        registro.marcarComoDuplicado();

        assertAll(
                () -> assertTrue(registro.estaDuplicado()),
                () -> assertTrue(
                        registro.foiIgnoradoComoDuplicado()
                ),
                () -> assertTrue(registro.estaFinalizado()),
                () -> assertEquals(
                        StatusRegistroOperacionalWebhook.DUPLICADO,
                        registro.getStatusProcessamento()
                ),
                () -> assertNotNull(
                        registro.getDataProcessamento()
                ),
                () -> assertNotNull(
                        registro.getDuracaoProcessamentoMs()
                )
        );
    }

    @Test
    void deveMarcarComoErroELimitarMensagem() {
        RegistroOperacionalWebhookPagamento registro =
                novoRegistro();

        String mensagemMuitoGrande = "x".repeat(2_500);

        registro.marcarComoErro(mensagemMuitoGrande);

        assertAll(
                () -> assertTrue(registro.estaComErro()),
                () -> assertTrue(registro.estaFinalizado()),
                () -> assertEquals(
                        StatusRegistroOperacionalWebhook.ERRO,
                        registro.getStatusProcessamento()
                ),
                () -> assertEquals(
                        2_000,
                        registro.getMensagemErro().length()
                ),
                () -> assertNotNull(
                        registro.getDataProcessamento()
                ),
                () -> assertNotNull(
                        registro.getDuracaoProcessamentoMs()
                )
        );
    }

    @Test
    void deveUsarMensagemPadraoQuandoErroNaoForInformado() {
        RegistroOperacionalWebhookPagamento registro =
                novoRegistro();

        registro.marcarComoErro(" ");

        assertEquals(
                "Erro não informado",
                registro.getMensagemErro()
        );
    }

    @Test
    void naoDeveExporPayloadOuErroNoToString() {
        String marcadorPayload =
                "conteudo-confidencial-do-payload";

        String marcadorErro =
                "detalhe-confidencial-do-erro";

        RegistroOperacionalWebhookPagamento registro =
                new RegistroOperacionalWebhookPagamento(
                        "evt-123",
                        "PIX-123",
                        StatusPagamento.APROVADO,
                        "{\"conteudo\":\""
                                + marcadorPayload
                                + "\"}",
                        "request-123",
                        "PAYMENT_UPDATED",
                        "FAKE_GATEWAY"
                );

        registro.marcarComoErro(marcadorErro);

        String resultado = registro.toString();

        assertAll(
                () -> assertFalse(
                        resultado.contains(marcadorPayload)
                ),
                () -> assertFalse(
                        resultado.contains(marcadorErro)
                ),
                () -> assertTrue(
                        resultado.contains("evt-123")
                ),
                () -> assertTrue(
                        resultado.contains("PIX-123")
                )
        );
    }

    @Test
    void naoDevePossuirCamposDiretosParaSegredos() {
        Set<String> nomesCampos =
                Arrays.stream(
                                RegistroOperacionalWebhookPagamento
                                        .class
                                        .getDeclaredFields()
                        )
                        .map(Field::getName)
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());

        assertAll(
                () -> assertFalse(
                        nomesCampos.stream()
                                .anyMatch(nome ->
                                        nome.contains("jwt")
                                )
                ),
                () -> assertFalse(
                        nomesCampos.stream()
                                .anyMatch(nome ->
                                        nome.contains("senha")
                                )
                ),
                () -> assertFalse(
                        nomesCampos.stream()
                                .anyMatch(nome ->
                                        nome.contains("refresh")
                                                && nome.contains("token")
                                )
                ),
                () -> assertFalse(
                        nomesCampos.stream()
                                .anyMatch(nome ->
                                        nome.contains("assinatura")
                                )
                ),
                () -> assertFalse(
                        nomesCampos.stream()
                                .anyMatch(nome ->
                                        nome.contains("secret")
                                )
                )
        );
    }

    @Test
    void naoDeveCriarRegistroSemEventId() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new RegistroOperacionalWebhookPagamento(
                                        " ",
                                        "PIX-123",
                                        StatusPagamento.APROVADO,
                                        "{}",
                                        "request-123",
                                        "PAYMENT_UPDATED",
                                        "FAKE_GATEWAY"
                                )
                );

        assertEquals(
                "EventId do webhook é obrigatório",
                exception.getMessage()
        );
    }

    @Test
    void naoDeveCriarRegistroSemPayload() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new RegistroOperacionalWebhookPagamento(
                                        "evt-123",
                                        "PIX-123",
                                        StatusPagamento.APROVADO,
                                        null,
                                        "request-123",
                                        "PAYMENT_UPDATED",
                                        "FAKE_GATEWAY"
                                )
                );

        assertEquals(
                "Payload original do webhook é obrigatório",
                exception.getMessage()
        );
    }

    @Test
    void naoDeveCriarRegistroSemStatusRecebido() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new RegistroOperacionalWebhookPagamento(
                                        "evt-123",
                                        "PIX-123",
                                        null,
                                        "{}",
                                        "request-123",
                                        "PAYMENT_UPDATED",
                                        "FAKE_GATEWAY"
                                )
                );

        assertEquals(
                "Status recebido é obrigatório",
                exception.getMessage()
        );
    }

    private RegistroOperacionalWebhookPagamento novoRegistro() {
        return new RegistroOperacionalWebhookPagamento(
                "evt-123",
                "PIX-123",
                StatusPagamento.APROVADO,
                """
                        {
                          "eventId": "evt-123",
                          "tipo": "PAYMENT_UPDATED",
                          "codigoTransacao": "PIX-123",
                          "statusPagamento": "APROVADO"
                        }
                        """,
                "request-123",
                "PAYMENT_UPDATED",
                "FAKE_GATEWAY"
        );
    }
}