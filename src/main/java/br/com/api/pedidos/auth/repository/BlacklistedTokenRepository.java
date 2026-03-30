package br.com.api.pedidos.auth.repository;

import br.com.api.pedidos.auth.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    boolean existsByToken(String token);
    void deleteByExpirationBefore(Date date);
}
