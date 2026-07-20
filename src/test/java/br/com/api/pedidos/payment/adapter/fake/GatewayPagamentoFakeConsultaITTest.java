package br.com.api.pedidos.payment.adapter.fake;

import br.com.api.pedidos.payment.adapter.fake.repository.TransacaoGatewayFakeRepository;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GatewayPagamentoFakeConsultaITTest {

    @Autowired
    private GatewayPagamentoFakeConsulta gatewayPagamentoFakeConsulta;

    @Autowired
    private TransacaoGatewayFakeRepository transacaoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveManterTransacaoPersistidaAposLimparContextoJpa() {
        String codigoTransacao = "PIX-TESTE-" + UUID.randomUUID();
        gatewayPagamentoFakeConsulta.registrarPagamentoPendente(codigoTransacao);

        assertTrue(
                transacaoRepository
                        .findByCodigoTransacao(codigoTransacao)
                        .isPresent()
        );

        /*
         * Remove as entidades do contexto de persistência.
         *
         * A próxima consulta não poderá reutilizar o objeto
         * que estava carregado em memória e precisará buscar
         * novamente no banco.
         */
        entityManager.flush();
        entityManager.clear();

        StatusPagamento status =
                gatewayPagamentoFakeConsulta
                        .consultarStatus(codigoTransacao);

        assertEquals(
                StatusPagamento.PENDENTE,
                status
        );
    }

    @Test
    void devePersistirAtualizacaoExternaDoStatus() {
        String codigoTransacao = "PIX-TESTE-" + UUID.randomUUID();
        gatewayPagamentoFakeConsulta.registrarPagamentoPendente(codigoTransacao);

        gatewayPagamentoFakeConsulta
                .simularAtualizacaoExterna(
                        codigoTransacao,
                        StatusPagamento.APROVADO
                );

        entityManager.flush();
        entityManager.clear();

        StatusPagamento status =
                gatewayPagamentoFakeConsulta
                        .consultarStatus(codigoTransacao);

        assertEquals(
                StatusPagamento.APROVADO,
                status
        );
    }
}
