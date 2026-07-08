package br.com.api.pedidos.payment.repository;

import br.com.api.pedidos.payment.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findAllByPedidoIdOrderByDataCriacaoDesc(Long idPedido);
    Optional<Pagamento> findByIdAndPedidoId(Long idPagamento, Long idPedido);
}
