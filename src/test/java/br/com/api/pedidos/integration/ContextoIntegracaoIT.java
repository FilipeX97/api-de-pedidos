package br.com.api.pedidos.integration;

import br.com.api.pedidos.integration.container.ContainersIntegracao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class ContextoIntegracaoIT extends ContainersIntegracao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void contextoSpringDeveSubirComPostgreSQLEMongoDBReais() {
        assertTrue(
                POSTGRESQL.isRunning()
        );

        assertTrue(
                MONGODB.isRunning()
        );

        assertTrue(
                mongoTemplate
                        .collectionExists(
                                "registro_operacional_webhook_pagamento"
                        )
        );
    }

}