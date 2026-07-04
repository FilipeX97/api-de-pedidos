package br.com.api.pedidos.notification.repository;

import br.com.api.pedidos.notification.entity.Notificacao;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findAllByUsuarioOrderByDataCriacaoDesc(Usuario usuario);
    Optional<Notificacao> findByIdAndUsuario(Long id, Usuario usuario);
    long countByUsuarioAndLidaFalse(Usuario usuario);

}
