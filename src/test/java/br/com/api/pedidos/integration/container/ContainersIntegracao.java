package br.com.api.pedidos.integration.container;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

public abstract class ContainersIntegracao {

    private static final String POSTGRES_DATABASE =
            "api_pedidos_integration";

    private static final String POSTGRES_USERNAME =
            "api_pedidos_test";

    private static final String POSTGRES_PASSWORD =
            "api_pedidos_test";

    private static final String MONGO_USERNAME =
            "api_pedidos_test";

    private static final String MONGO_PASSWORD =
            "api_pedidos_test";

    private static final String MONGO_DATABASE =
            "api_pedidos_operacional";

    protected static final String RABBITMQ_USERNAME =
            "api_pedidos_test";

    protected static final String RABBITMQ_PASSWORD =
            "api_pedidos_test";

    private static final String INTEGRATION_JWT_SECRET =
            "chave-jwt-integracao-api-pedidos-" +
                    "2026-chave-segura-teste-" +
                    "nao-utilizar-em-producao-" +
                    "01234567890123456789";

    private static final String INTEGRATION_WEBHOOK_SECRET =
            "segredo-webhook-integracao-" +
                    "api-pedidos-" +
                    "nao-utilizar-em-producao";

    protected static final PostgreSQLContainer<?> POSTGRESQL =
            new PostgreSQLContainer<>(
                    "postgres:16-alpine"
            )
                    .withDatabaseName(
                            POSTGRES_DATABASE
                    )
                    .withUsername(
                            POSTGRES_USERNAME
                    )
                    .withPassword(
                            POSTGRES_PASSWORD
                    );

    protected static final MongoDBContainer MONGODB =
            new MongoDBContainer(
                    "mongo:8.0.28-noble"
            )
                    .withEnv(
                            "MONGO_INITDB_ROOT_USERNAME",
                            MONGO_USERNAME
                    )
                    .withEnv(
                            "MONGO_INITDB_ROOT_PASSWORD",
                            MONGO_PASSWORD
                    )
                    .withEnv(
                            "MONGO_INITDB_DATABASE",
                            MONGO_DATABASE
                    );

    protected static final RabbitMQContainer RABBITMQ =
            new RabbitMQContainer(
                    "rabbitmq:4.3.5-management"
            )
                    .withAdminUser(
                            RABBITMQ_USERNAME
                    )
                    .withAdminPassword(
                            RABBITMQ_PASSWORD
                    );

    static {
        POSTGRESQL.start();
        MONGODB.start();
        RABBITMQ.start();
    }

    @Autowired
    private LimpezaDadosIntegracao limpezaDadosIntegracao;

    @BeforeEach
    void limparDadosAntesDoTeste() {
        limpezaDadosIntegracao.limpar();
    }

    @DynamicPropertySource
    static void configurarPropriedades(
            DynamicPropertyRegistry registry
    ) {
        configurarPostgreSQL(registry);
        configurarMongoDB(registry);
        configurarRabbitMQ(registry);
        configurarSeguranca(registry);
    }

    private static void configurarPostgreSQL(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRESQL::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                POSTGRESQL::getUsername
        );

        registry.add(
                "spring.datasource.password",
                POSTGRESQL::getPassword
        );

        registry.add(
                "spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver"
        );
    }

    private static void configurarMongoDB(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.data.mongodb.host",
                MONGODB::getHost
        );

        registry.add(
                "spring.data.mongodb.port",
                MONGODB::getFirstMappedPort
        );

        registry.add(
                "spring.data.mongodb.database",
                () -> MONGO_DATABASE
        );

        registry.add(
                "spring.data.mongodb.username",
                () -> MONGO_USERNAME
        );

        registry.add(
                "spring.data.mongodb.password",
                () -> MONGO_PASSWORD
        );

        registry.add(
                "spring.data.mongodb.authentication-database",
                () -> "admin"
        );
    }

    private static void configurarRabbitMQ(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.rabbitmq.host",
                RABBITMQ::getHost
        );

        registry.add(
                "spring.rabbitmq.port",
                RABBITMQ::getAmqpPort
        );

        registry.add(
                "spring.rabbitmq.username",
                RABBITMQ::getAdminUsername
        );

        registry.add(
                "spring.rabbitmq.password",
                RABBITMQ::getAdminPassword
        );

        registry.add(
                "spring.rabbitmq.virtual-host",
                () -> "/"
        );
    }

    private static void configurarSeguranca(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "api.security.token.chave-secreta",
                () -> INTEGRATION_JWT_SECRET
        );

        registry.add(
                "api.security.token.expiracao",
                () -> "900000"
        );

        registry.add(
                "api.security.token.expiracao-refresh",
                () -> "604800000"
        );

        registry.add(
                "api.security.token.tempo-antes-expiracao-para-renovar",
                () -> "300000"
        );

        registry.add(
                "api.webhook.fake.secret",
                () -> INTEGRATION_WEBHOOK_SECRET
        );
    }
}