package br.com.api.pedidos.messaging.service;

import br.com.api.pedidos.messaging.repository.MensagemProcessadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MensagemProcessadaService {

    private final MensagemProcessadaRepository repository;

    public MensagemProcessadaService(
            MensagemProcessadaRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public boolean processar(
            UUID idMensagem,
            String tipoMensagem,
            Runnable processamento
    ) {
        if (idMensagem == null) {
            throw new IllegalArgumentException(
                    "Id da mensagem é obrigatório"
            );
        }

        if (tipoMensagem == null || tipoMensagem.isBlank()) {
            throw new IllegalArgumentException(
                    "Tipo da mensagem é obrigatório"
            );
        }

        int inserido = repository.registrarSeAindaNaoProcessada(
                idMensagem,
                tipoMensagem,
                LocalDateTime.now()
        );

        if (inserido == 0) {
            return false;
        }

        processamento.run();

        return true;
    }
}
