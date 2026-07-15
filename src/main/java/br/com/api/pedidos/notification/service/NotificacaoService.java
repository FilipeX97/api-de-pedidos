package br.com.api.pedidos.notification.service;

import br.com.api.pedidos.notification.dto.NotificacaoResponseDTO;
import br.com.api.pedidos.notification.entity.Notificacao;
import br.com.api.pedidos.notification.entity.TipoNotificacao;
import br.com.api.pedidos.notification.repository.NotificacaoRepository;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.pagination.util.PaginacaoUtils;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class NotificacaoService {

    private static final Map<String, String> CAMPOS_ORDENACAO =
            Map.ofEntries(
                    Map.entry("id", "id"),
                    Map.entry("titulo", "titulo"),
                    Map.entry("tipo", "tipo"),
                    Map.entry("lida", "lida"),
                    Map.entry("dataCriacao", "dataCriacao")
            );

    private static final Sort ORDENACAO_PADRAO =
            Sort.by(
                    Sort.Order.desc("dataCriacao")
            );

    private final NotificacaoRepository notificacaoRepository;
    private final PedidoRepository pedidoRepository;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            PedidoRepository pedidoRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void criar(
            Long idPedido,
            String titulo,
            String mensagem,
            TipoNotificacao tipo) {
        if (idPedido == null) {
            throw new IllegalArgumentException("Id do pedido é obrigatório");
        }

        var pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() ->
                        new RuntimeException("Pedido não encontrado")
                );

        var usuarioDonoDoPedido = pedido.getUsuario();

        notificacaoRepository.saveAndFlush(
                new Notificacao(
                        usuarioDonoDoPedido,
                        pedido,
                        titulo,
                        mensagem,
                        tipo
                )
        );
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<NotificacaoResponseDTO> listarPorUsuario(
            Usuario usuario,
            Boolean lida,
            Pageable pageable
    ) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }

        Pageable pageableValidado =
                PaginacaoUtils.normalizar(
                        pageable,
                        CAMPOS_ORDENACAO,
                        ORDENACAO_PADRAO
                );

        Page<Notificacao> pagina;

        if (lida == null) {
            pagina = notificacaoRepository.findAllByUsuario(
                    usuario,
                    pageableValidado
            );
        } else {
            pagina = notificacaoRepository
                    .findAllByUsuarioAndLida(
                            usuario,
                            lida,
                            pageableValidado
                    );
        }

        return PaginaResponseDTO.from(
                pagina.map(NotificacaoResponseDTO::from)
        );
    }

    @Transactional(readOnly = true)
    public Long contarNaoLidas(Usuario usuario) {
        return notificacaoRepository.countByUsuarioAndLidaFalse(usuario);
    }

    @Transactional
    public NotificacaoResponseDTO marcarComoLida(Long idNotificacao, Usuario usuario) {
        var notificacao = notificacaoRepository
                .findByIdAndUsuario(idNotificacao, usuario)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));

        notificacao.marcarComoLida();
        return NotificacaoResponseDTO.from(notificacao);
    }
}
