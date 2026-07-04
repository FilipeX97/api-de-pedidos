package br.com.api.pedidos.audit.repository;

import br.com.api.pedidos.audit.entity.Auditoria;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findAllByPedidoIdOrderByDataCriacaoDesc(Long idPedido);
    List<Auditoria> findAllByUsuarioOrderByDataCriacaoDesc(Usuario usuario);
}
