package br.com.api.pedidos.auth.repository;

import br.com.api.pedidos.auth.entity.RefreshToken;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteAllByUsuario(Usuario usuario);
}
