package br.com.api.pedidos.notification.service;

import br.com.api.pedidos.notification.dto.NotificacaoResponseDTO;
import br.com.api.pedidos.notification.entity.Notificacao;
import br.com.api.pedidos.notification.entity.TipoNotificacao;
import br.com.api.pedidos.notification.repository.NotificacaoRepository;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoService {

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

        notificacaoRepository.save(
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
    public List<NotificacaoResponseDTO> listarPorUsuario(Usuario usuario) {
        return notificacaoRepository
                .findAllByUsuarioOrderByDataCriacaoDesc(usuario)
                .stream().map(NotificacaoResponseDTO::from).toList();
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
