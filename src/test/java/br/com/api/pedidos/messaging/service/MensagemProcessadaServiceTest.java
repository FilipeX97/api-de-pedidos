package br.com.api.pedidos.messaging.service;

import br.com.api.pedidos.messaging.repository.MensagemProcessadaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MensagemProcessadaServiceTest {

    @Test
    void deveProcessarMensagemNova() {
        MensagemProcessadaRepository repository =
                mock(MensagemProcessadaRepository.class);

        MensagemProcessadaService service =
                new MensagemProcessadaService(repository);

        UUID idMensagem = UUID.randomUUID();

        when(
                repository.registrarSeAindaNaoProcessada(
                        eq(idMensagem),
                        eq("PEDIDO_PAGO"),
                        org.mockito.ArgumentMatchers.any(LocalDateTime.class)
                )
        ).thenReturn(1);

        Runnable processamento = mock(Runnable.class);

        boolean resultado =
                service.processar(
                        idMensagem,
                        "PEDIDO_PAGO",
                        processamento
                );

        assertTrue(resultado);

        verify(processamento).run();
    }

    @Test
    void naoDeveProcessarMensagemDuplicada() {
        MensagemProcessadaRepository repository =
                mock(MensagemProcessadaRepository.class);

        MensagemProcessadaService service =
                new MensagemProcessadaService(repository);

        UUID idMensagem = UUID.randomUUID();

        when(
                repository.registrarSeAindaNaoProcessada(
                        eq(idMensagem),
                        eq("PEDIDO_PAGO"),
                        org.mockito.ArgumentMatchers.any(LocalDateTime.class)
                )
        ).thenReturn(0);

        Runnable processamento = mock(Runnable.class);

        boolean resultado =
                service.processar(
                        idMensagem,
                        "PEDIDO_PAGO",
                        processamento
                );

        assertFalse(resultado);

        verify(processamento, never()).run();
    }

    @Test
    void deveRejeitarIdDaMensagemNulo() {
        MensagemProcessadaRepository repository =
                mock(MensagemProcessadaRepository.class);

        MensagemProcessadaService service =
                new MensagemProcessadaService(repository);

        Runnable processamento = mock(Runnable.class);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.processar(
                        null,
                        "PEDIDO_PAGO",
                        processamento
                )
        );

        verify(processamento, never()).run();
    }

    @Test
    void deveRejeitarTipoDaMensagemVazio() {
        MensagemProcessadaRepository repository =
                mock(MensagemProcessadaRepository.class);

        MensagemProcessadaService service =
                new MensagemProcessadaService(repository);

        UUID idMensagem = UUID.randomUUID();

        Runnable processamento = mock(Runnable.class);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.processar(
                        idMensagem,
                        " ",
                        processamento
                )
        );

        verify(processamento, never()).run();
    }
}
