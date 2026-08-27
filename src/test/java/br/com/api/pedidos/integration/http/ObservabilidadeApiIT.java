package br.com.api.pedidos.integration.http;

import br.com.api.pedidos.integration.container.ContainersIntegracao;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class ObservabilidadeApiIT extends ContainersIntegracao {

    @LocalServerPort
    private int porta;

    @Test
    void healthDeveResponderComStatusUp() {

        RestAssuredIntegracao
                .requisicao(porta)
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .contentType(
                        org.hamcrest.Matchers
                                .containsString("application/json")
                )
                .body(
                        "status",
                        equalTo("UP")
                );
    }

    @Test
    void readinessDeveResponderComStatusUp() {

        RestAssuredIntegracao
                .requisicao(porta)
                .when()
                .get("/actuator/health/readiness")
                .then()
                .statusCode(200)
                .contentType(
                        org.hamcrest.Matchers
                                .containsString("application/json")
                )
                .body(
                        "status",
                        equalTo("UP")
                );
    }

    @Test
    void prometheusDeveResponderSemToken() {

        RestAssuredIntegracao
                .requisicaoTexto(porta)
                .when()
                .get("/actuator/prometheus")
                .then()
                .statusCode(200)
                .contentType(
                        containsString("text/plain")
                )
                .body(
                        containsString(
                                "jvm_memory_used_bytes"
                        )
                )
                .body(
                        containsString(
                                "http_server_requests_seconds"
                        )
                );
    }

    @Test
    void apiDeveRetornarRequestId() {

        Response resposta =
                RestAssuredIntegracao
                        .requisicao(porta)
                        .when()
                        .get("/actuator/health");

        resposta
                .then()
                .statusCode(200)
                .header(
                        "X-Request-Id",
                        notNullValue()
                );
    }

    @Test
    void apiDevePreservarRequestIdValido() {

        String requestId =
                "integration-request-id-123";

        RestAssuredIntegracao
                .requisicao(porta)
                .header(
                        "X-Request-Id",
                        requestId
                )
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .header(
                        "X-Request-Id",
                        equalTo(requestId)
                );
    }
}