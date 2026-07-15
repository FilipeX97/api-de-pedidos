package br.com.api.pedidos.notification.controller;

import br.com.api.pedidos.notification.dto.NotificacaoResponseDTO;
import br.com.api.pedidos.notification.service.NotificacaoService;
import br.com.api.pedidos.security.service.UsuarioLogadoService;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.response.RespostaApi;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

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
    public RespostaApi<PaginaResponseDTO<NotificacaoResponseDTO>> listarMinhasNotificacoes(
            @RequestParam(required = false)
            Boolean lida,

            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "dataCriacao",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                notificacaoService.listarPorUsuario(
                        usuario,
                        lida,
                        pageable
                ),
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
