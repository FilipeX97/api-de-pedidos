package br.com.api.pedidos.notification.repository;

import br.com.api.pedidos.notification.entity.Notificacao;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    @EntityGraph(attributePaths = {
            "usuario",
            "pedido"
    })
    Page<Notificacao> findAllByUsuario(Usuario usuario, Pageable pageable);

    @EntityGraph(attributePaths = {
            "usuario",
            "pedido"
    })
    Page<Notificacao> findAllByUsuarioAndLida(Usuario usuario, boolean lida, Pageable pageable);

    Optional<Notificacao> findByIdAndUsuario(Long id, Usuario usuario);
    long countByUsuarioAndLidaFalse(Usuario usuario);

}
