package br.com.api.pedidos.order.history.repository;

import br.com.api.pedidos.order.history.entity.HistoricoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoPedidoRepository extends JpaRepository<HistoricoPedido, Long> {
    List<HistoricoPedido> findAllByPedidoIdOrderByDataCriacaoDesc(Long pedidoId);
}
