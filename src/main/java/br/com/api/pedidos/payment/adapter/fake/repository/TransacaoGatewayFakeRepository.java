package br.com.api.pedidos.payment.adapter.fake.repository;

import br.com.api.pedidos.payment.adapter.fake.entity.TransacaoGatewayFake;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransacaoGatewayFakeRepository extends JpaRepository<TransacaoGatewayFake, Long> {
    Optional<TransacaoGatewayFake> findByCodigoTransacao(String codigoTransacao);
    boolean existsByCodigoTransacao(String codigoTransacao);
}
