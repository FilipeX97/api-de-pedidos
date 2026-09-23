package br.com.api.pedidos.messaging.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PedidoEventoKafkaMensagem(
        UUID idEvento,
        Integer versao,
        String tipoEvento,
        Long idPedido,
        Long idUsuario,
        BigDecimal valorFinal,
        LocalDateTime dataHoraEvento,
        String statusPedido
) {
}
