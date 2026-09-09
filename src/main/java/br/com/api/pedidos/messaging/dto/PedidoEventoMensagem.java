package br.com.api.pedidos.messaging.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PedidoEventoMensagem(
        UUID idEvento,
        String tipoEvento,
        Long idPedido,
        Long idUsuario,
        BigDecimal valor,
        LocalDateTime dataHora,
        String statusNovo
) {
    public PedidoEventoMensagem(
            UUID idEvento,
            String tipoEvento,
            Long idPedido,
            Long idUsuario,
            BigDecimal valor,
            LocalDateTime dataHora
    ) {
        this(
                idEvento,
                tipoEvento,
                idPedido,
                idUsuario,
                valor,
                dataHora,
                null
        );
    }
}
