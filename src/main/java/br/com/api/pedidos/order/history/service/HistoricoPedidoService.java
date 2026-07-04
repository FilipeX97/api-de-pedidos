package br.com.api.pedidos.order.history.service;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.history.dto.HistoricoPedidoResponseDTO;
import br.com.api.pedidos.order.history.entity.HistoricoPedido;
import br.com.api.pedidos.order.history.repository.HistoricoPedidoRepository;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.order.state.StatusPedido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoricoPedidoService {

    private final HistoricoPedidoRepository historicoPedidoRepository;
    private final PedidoRepository pedidoRepository;

    public HistoricoPedidoService(
            HistoricoPedidoRepository historicoPedidoRepository,
            PedidoRepository pedidoRepository) {
        this.historicoPedidoRepository = historicoPedidoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long idPedido, StatusPedido status, String descricao) {
        Pedido pedido = buscarPedido(idPedido);

        historicoPedidoRepository.saveAndFlush(
                new HistoricoPedido(pedido, status, descricao)
        );
    }

    @Transactional(readOnly = true)
    public List<HistoricoPedidoResponseDTO> listarPorPedido(Long idPedido) {
        return historicoPedidoRepository
                .findAllByPedidoIdOrderByDataCriacaoDesc(idPedido)
                .stream()
                .map(HistoricoPedidoResponseDTO::from)
                .toList();
    }

    private Pedido buscarPedido(Long idPedido) {
        if (idPedido == null) {
            throw new IllegalArgumentException("Id do pedido é obrigatório");
        }

        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }
}
