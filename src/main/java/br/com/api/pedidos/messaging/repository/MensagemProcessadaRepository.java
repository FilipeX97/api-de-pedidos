package br.com.api.pedidos.messaging.repository;

import br.com.api.pedidos.messaging.entity.MensagemProcessada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.UUID;

public interface MensagemProcessadaRepository
        extends JpaRepository<MensagemProcessada, UUID> {

    boolean existsByIdMensagem(UUID idMensagem);

    @Modifying
    @Query(
            value = """
                    INSERT INTO mensagem_processada (
                        id_mensagem,
                        tipo_mensagem,
                        data_processamento
                    )
                    VALUES (
                        :idMensagem,
                        :tipoMensagem,
                        :dataProcessamento
                    )
                    ON CONFLICT (id_mensagem) DO NOTHING
                    """,
            nativeQuery = true
    )
    int registrarSeAindaNaoProcessada(
            UUID idMensagem,
            String tipoMensagem,
            LocalDateTime dataProcessamento
    );
}
