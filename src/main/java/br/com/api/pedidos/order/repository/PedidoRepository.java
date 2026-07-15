package br.com.api.pedidos.order.repository;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository
        extends JpaRepository<Pedido, Long>,
        JpaSpecificationExecutor<Pedido> {
    Optional<Pedido> findByIdAndUsuario(Long id, Usuario usuario);

    @EntityGraph(attributePaths = {
            "cupom"
    })
    Page<Pedido> findAllByUsuario(
            Usuario usuario,
            Pageable pageable
    );

    @Override
    @EntityGraph(attributePaths = {
            "usuario",
            "cupom"
    })
    Page<Pedido> findAll(
            Specification<Pedido> specification,
            Pageable pageable
    );
}
