package br.com.api.pedidos.integration.http;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public final class RestAssuredIntegracao {

    private RestAssuredIntegracao() {
    }

    public static RequestSpecification requisicao(int porta) {
        return RestAssured
                .given()
                .port(porta)
                .header(
                        "User-Agent",
                        "api-de-pedidos-integracao-test"
                )
                .accept("application/json");
    }

    public static RequestSpecification requisicaoTexto(int porta) {
        return RestAssured
                .given()
                .port(porta)
                .header(
                        "User-Agent",
                        "api-de-pedidos-integracao-test"
                )
                .accept("text/plain");
    }
}