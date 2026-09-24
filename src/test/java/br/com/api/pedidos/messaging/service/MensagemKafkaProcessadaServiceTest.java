package br.com.api.pedidos.messaging.service;

import br.com.api.pedidos.messaging.entity.MensagemKafkaProcessada;
import br.com.api.pedidos.messaging.repository.MensagemKafkaProcessadaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MensagemKafkaProcessadaServiceTest {

    @Test
    void deveProcessarPrimeiraMensagem() {
        MensagemKafkaProcessadaRepository repository =
                mock(MensagemKafkaProcessadaRepository.class);

        MensagemKafkaProcessadaService service =
                new MensagemKafkaProcessadaService(repository);

        UUID idEvento = UUID.randomUUID();

        when(
                repository.existsById_IdEventoAndId_GrupoConsumidor(
                        idEvento,
                        "integracao"
                )
        ).thenReturn(false);

        AtomicInteger quantidadeProcessamentos =
                new AtomicInteger();

        boolean processou = service.processar(
                idEvento,
                "integracao",
                quantidadeProcessamentos::incrementAndGet
        );

        assertTrue(processou);
        assertEquals(1, quantidadeProcessamentos.get());

        verify(repository).saveAndFlush(
                any(MensagemKafkaProcessada.class)
        );
    }

    @Test
    void deveIgnorarMensagemJaProcessadaNoMesmoGrupo() {
        MensagemKafkaProcessadaRepository repository =
                mock(MensagemKafkaProcessadaRepository.class);

        MensagemKafkaProcessadaService service =
                new MensagemKafkaProcessadaService(repository);

        UUID idEvento = UUID.randomUUID();

        when(
                repository.existsById_IdEventoAndId_GrupoConsumidor(
                        idEvento,
                        "integracao"
                )
        ).thenReturn(
                false,
                true
        );

        AtomicInteger quantidadeProcessamentos =
                new AtomicInteger();

        boolean primeiraExecucao = service.processar(
                idEvento,
                "integracao",
                quantidadeProcessamentos::incrementAndGet
        );

        boolean segundaExecucao = service.processar(
                idEvento,
                "integracao",
                quantidadeProcessamentos::incrementAndGet
        );

        assertTrue(primeiraExecucao);
        assertFalse(segundaExecucao);
        assertEquals(1, quantidadeProcessamentos.get());

        verify(repository).saveAndFlush(
                any(MensagemKafkaProcessada.class)
        );
    }

    @Test
    void devePermitirMesmoEventoEmGruposDiferentes() {
        MensagemKafkaProcessadaRepository repository =
                mock(MensagemKafkaProcessadaRepository.class);

        MensagemKafkaProcessadaService service =
                new MensagemKafkaProcessadaService(repository);

        UUID idEvento = UUID.randomUUID();

        when(
                repository.existsById_IdEventoAndId_GrupoConsumidor(
                        idEvento,
                        "integracao"
                )
        ).thenReturn(false);

        when(
                repository.existsById_IdEventoAndId_GrupoConsumidor(
                        idEvento,
                        "analytics"
                )
        ).thenReturn(false);

        AtomicInteger quantidadeProcessamentos =
                new AtomicInteger();

        boolean processouIntegracao = service.processar(
                idEvento,
                "integracao",
                quantidadeProcessamentos::incrementAndGet
        );

        boolean processouAnalytics = service.processar(
                idEvento,
                "analytics",
                quantidadeProcessamentos::incrementAndGet
        );

        assertTrue(processouIntegracao);
        assertTrue(processouAnalytics);
        assertEquals(2, quantidadeProcessamentos.get());

        ArgumentCaptor<MensagemKafkaProcessada> captor =
                ArgumentCaptor.forClass(
                        MensagemKafkaProcessada.class
                );

        verify(repository, org.mockito.Mockito.times(2))
                .saveAndFlush(captor.capture());

        var mensagensSalvas = captor.getAllValues();

        assertEquals(
                idEvento,
                mensagensSalvas.get(0)
                        .getId()
                        .getIdEvento()
        );

        assertEquals(
                idEvento,
                mensagensSalvas.get(1)
                        .getId()
                        .getIdEvento()
        );

        assertEquals(
                "integracao",
                mensagensSalvas.get(0)
                        .getId()
                        .getGrupoConsumidor()
        );

        assertEquals(
                "analytics",
                mensagensSalvas.get(1)
                        .getId()
                        .getGrupoConsumidor()
        );
    }

    @Test
    void devePropagarExcecaoDoProcessamento() {
        MensagemKafkaProcessadaRepository repository =
                mock(MensagemKafkaProcessadaRepository.class);

        MensagemKafkaProcessadaService service =
                new MensagemKafkaProcessadaService(repository);

        UUID idEvento = UUID.randomUUID();

        when(
                repository.existsById_IdEventoAndId_GrupoConsumidor(
                        idEvento,
                        "integracao"
                )
        ).thenReturn(false);

        IllegalStateException excecao =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.processar(
                                idEvento,
                                "integracao",
                                () -> {
                                    throw new IllegalStateException(
                                            "Falha no processamento"
                                    );
                                }
                        )
                );

        assertEquals(
                "Falha no processamento",
                excecao.getMessage()
        );

        verify(repository).saveAndFlush(
                any(MensagemKafkaProcessada.class)
        );
    }

    @Test
    void devePermitirNovaTentativaAposFalha() {
        MensagemKafkaProcessadaRepository repository =
                mock(MensagemKafkaProcessadaRepository.class);

        MensagemKafkaProcessadaService service =
                new MensagemKafkaProcessadaService(repository);

        UUID idEvento = UUID.randomUUID();

        when(
                repository.existsById_IdEventoAndId_GrupoConsumidor(
                        idEvento,
                        "integracao"
                )
        ).thenReturn(false, false);

        AtomicInteger quantidadeProcessamentos =
                new AtomicInteger();

        assertThrows(
                IllegalStateException.class,
                () -> service.processar(
                        idEvento,
                        "integracao",
                        () -> {
                            quantidadeProcessamentos.incrementAndGet();

                            throw new IllegalStateException(
                                    "Falha temporaria"
                            );
                        }
                )
        );

        boolean segundaExecucao = service.processar(
                idEvento,
                "integracao",
                quantidadeProcessamentos::incrementAndGet
        );

        assertTrue(segundaExecucao);
        assertEquals(2, quantidadeProcessamentos.get());

        verify(
                repository,
                org.mockito.Mockito.times(2)
        ).saveAndFlush(
                any(MensagemKafkaProcessada.class)
        );
    }
}