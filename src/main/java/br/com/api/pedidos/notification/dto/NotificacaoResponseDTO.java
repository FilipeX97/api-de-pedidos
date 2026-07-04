package br.com.api.pedidos.notification.dto;

import br.com.api.pedidos.notification.entity.Notificacao;
import br.com.api.pedidos.notification.entity.TipoNotificacao;

import java.time.LocalDateTime;

public record NotificacaoResponseDTO(
        Long id,
        Long idUsuario,
        Long idPedido,
        String titulo,
        String mensagem,
        TipoNotificacao tipo,
        boolean lida,
        LocalDateTime dataCriacao) {
    public static NotificacaoResponseDTO from(Notificacao notificacao) {
        return new NotificacaoResponseDTO(
                notificacao.getId(),
                notificacao.getIdUsuario(),
                notificacao.getIdPedido(),
                notificacao.getTitulo(),
                notificacao.getMensagem(),
                notificacao.getTipo(),
                notificacao.isLida(),
                notificacao.getDataCriacao()
        );
    }
}
