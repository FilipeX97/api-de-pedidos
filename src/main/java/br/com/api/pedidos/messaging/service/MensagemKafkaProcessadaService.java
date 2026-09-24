package br.com.api.pedidos.messaging.service;

import br.com.api.pedidos.messaging.entity.MensagemKafkaProcessada;
import br.com.api.pedidos.messaging.repository.MensagemKafkaProcessadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MensagemKafkaProcessadaService {

    private final MensagemKafkaProcessadaRepository repository;

    public MensagemKafkaProcessadaService(
            MensagemKafkaProcessadaRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public boolean processar(
            UUID idEvento,
            String grupoConsumidor,
            Runnable processamento
    ) {
        validar(idEvento, grupoConsumidor, processamento);

        if (repository.existsById_IdEventoAndId_GrupoConsumidor(
                idEvento,
                grupoConsumidor
        )) {
            return false;
        }

        repository.saveAndFlush(
                new MensagemKafkaProcessada(
                        idEvento,
                        grupoConsumidor,
                        LocalDateTime.now()
                )
        );

        processamento.run();

        return true;
    }

    private void validar(
            UUID idEvento,
            String grupoConsumidor,
            Runnable processamento
    ) {
        if (idEvento == null) {
            throw new IllegalArgumentException("Id do evento e obrigatorio");
        }

        if (grupoConsumidor == null || grupoConsumidor.isBlank()) {
            throw new IllegalArgumentException("Grupo consumidor e obrigatorio");
        }

        if (processamento == null) {
            throw new IllegalArgumentException("Processamento e obrigatorio");
        }
    }
}
