package br.com.api.pedidos.audit.service;

import br.com.api.pedidos.audit.entity.Auditoria;
import br.com.api.pedidos.audit.entity.TipoAcao;
import br.com.api.pedidos.audit.repository.AuditoriaRepository;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    private static final String RECURSO_PEDIDO = "PEDIDO";

    private final AuditoriaRepository auditoriaRepository;
    private final PedidoRepository pedidoRepository;

    public AuditoriaService(
            AuditoriaRepository auditoriaRepository,
            PedidoRepository pedidoRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(
            Long idPedido,
            TipoAcao acao,
            String descricao) {
        if (idPedido == null) {
            throw new IllegalArgumentException("Id do pedido é obrigatório");
        }

        var pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        var usuarioDonoDoPedido = pedido.getUsuario();

        auditoriaRepository.save(
                new Auditoria(
                        usuarioDonoDoPedido,
                        pedido,
                        RECURSO_PEDIDO,
                        acao,
                        descricao
                )
        );
    }
}
