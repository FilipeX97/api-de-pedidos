package br.com.api.pedidos.integration.container;

import br.com.api.pedidos.payment.webhook.document.entity.RegistroOperacionalWebhookPagamento;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collection;

@Component
public class LimpezaDadosIntegracao {

    private static final String COLECAO_WEBHOOK_OPERACIONAL =
            "registro_operacional_webhook_pagamento";

    private static final String SQL_LIMPAR_DADOS =
            "TRUNCATE TABLE " +
                    "item_pedido, " +
                    "historico_pedido, " +
                    "notificacao, " +
                    "auditoria, " +
                    "pagamento, " +
                    "webhook_pagamento_recebido, " +
                    "transacao_gateway_fake, " +
                    "refresh_token, " +
                    "blacklisted_token, " +
                    "idempotency_key, " +
                    "pedido, " +
                    "cupom, " +
                    "produto, " +
                    "usuario " +
                    "RESTART IDENTITY CASCADE";

    private final JdbcTemplate jdbcTemplate;
    private final MongoTemplate mongoTemplate;
    private final CacheManager cacheManager;

    public LimpezaDadosIntegracao(
            JdbcTemplate jdbcTemplate,
            MongoTemplate mongoTemplate,
            CacheManager cacheManager
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.mongoTemplate = mongoTemplate;
        this.cacheManager = cacheManager;
    }

    public void limpar() {
        limparPostgreSQL();
        limparMongoDB();
        limparCaches();
    }

    private void limparPostgreSQL() {
        jdbcTemplate.execute(SQL_LIMPAR_DADOS);
    }

    private void limparMongoDB() {
        if (!mongoTemplate.collectionExists(
                COLECAO_WEBHOOK_OPERACIONAL
        )) {
            return;
        }

        mongoTemplate.remove(
                new Query(),
                RegistroOperacionalWebhookPagamento.class
        );
    }

    private void limparCaches() {
        Collection<String> nomesCaches =
                cacheManager.getCacheNames();

        for (String nomeCache : nomesCaches) {
            Cache cache = cacheManager.getCache(nomeCache);

            if (cache != null) {
                cache.clear();
            }
        }
    }
}