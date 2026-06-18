package br.com.api.pedidos.order.repository;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByIdAndUsuario(Long id, Usuario usuario);
    List<Pedido> findAllByUsuario(Usuario usuario);
}
