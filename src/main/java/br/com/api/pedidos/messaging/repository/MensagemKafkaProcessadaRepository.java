package br.com.api.pedidos.messaging.repository;

import br.com.api.pedidos.messaging.entity.MensagemKafkaProcessada;
import br.com.api.pedidos.messaging.entity.MensagemKafkaProcessadaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensagemKafkaProcessadaRepository
        extends JpaRepository<MensagemKafkaProcessada, MensagemKafkaProcessadaId> {
    boolean existsById_IdEventoAndId_GrupoConsumidor(
            java.util.UUID idEvento,
            String grupoConsumidor
    );
}
