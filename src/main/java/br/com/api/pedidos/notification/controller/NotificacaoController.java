package br.com.api.pedidos.notification.controller;

import br.com.api.pedidos.notification.dto.NotificacaoResponseDTO;
import br.com.api.pedidos.notification.service.NotificacaoService;
import br.com.api.pedidos.security.service.UsuarioLogadoService;
import br.com.api.pedidos.shared.response.RespostaApi;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final UsuarioLogadoService usuarioLogadoService;

    public NotificacaoController(
            NotificacaoService notificacaoService,
            UsuarioLogadoService usuarioLogadoService) {
        this.notificacaoService = notificacaoService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    @GetMapping
    public RespostaApi<List<NotificacaoResponseDTO>> listarMinhasNotificacoes() {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                notificacaoService.listarPorUsuario(usuario),
                "Notificações encontradas"
        );
    }

    @GetMapping("/unread-count")
    public RespostaApi<Long> contarNaoLidas() {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                notificacaoService.contarNaoLidas(usuario),
                "Quantidade de notificações não lidas encontrada"
        );
    }

    @PatchMapping("/{idNotificacao}/read")
    public RespostaApi<NotificacaoResponseDTO> marcarComoLida(
            @PathVariable Long idNotificacao) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                notificacaoService.marcarComoLida(idNotificacao, usuario),
                "Notificação marcada como lida"
        );
    }
}
