package br.com.api.pedidos.integration.auth;

import br.com.api.pedidos.integration.container.ContainersIntegracao;
import br.com.api.pedidos.security.ratelimit.RateLimitService;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static br.com.api.pedidos.integration.http.RestAssuredIntegracao.requisicao;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class AutenticacaoApiIT extends ContainersIntegracao {

    private static final String USER_AGENT = "api-de-pedidos-integracao-test";
    private static final String EMAIL = "integracao@api-pedidos.com";
    private static final String SENHA = "SenhaIntegracao123";

    @LocalServerPort
    private int porta;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RateLimitService rateLimitService;

    @BeforeEach
    void prepararUsuario() {
        Mockito.when(rateLimitService.permitirRequisicao(Mockito.anyString()))
                .thenReturn(true);

        Usuario usuario =
                new Usuario(
                        "Usuario Integração",
                        EMAIL,
                        passwordEncoder.encode(SENHA),
                        Perfil.USER
                );

        usuarioRepository.saveAndFlush(usuario);
    }

    @Test
    void deveRealizarLoginComCredenciaisValidas() {
        String payload = """
                {
                  "email": "%s",
                  "senha": "%s"
                }
                """.formatted(
                EMAIL,
                SENHA
        );

        requisicao(porta)
                .header(
                        "User-Agent",
                        USER_AGENT
                )
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body(
                        "sucesso",
                        equalTo(true)
                )
                .body(
                        "dados.accessToken",
                        notNullValue()
                )
                .body(
                        "dados.refreshToken",
                        notNullValue()
                )
                .body(
                        "mensagem",
                        equalTo(
                                "Login realizado com sucesso"
                        )
                );
    }

    @Test
    void deveRetornarErroQuandoSenhaEstiverInvalida() {
        String payload = """
                {
                  "email": "%s",
                  "senha": "SenhaErrada123"
                }
                """.formatted(
                EMAIL
        );

        requisicao(porta)
                .header(
                        "User-Agent",
                        USER_AGENT
                )
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400)
                .body(
                        "sucesso",
                        equalTo(false)
                )
                .body(
                        "mensagem",
                        equalTo("Senha incorreta")
                );
    }

    @Test
    void deveRetornarBadRequestQuandoLoginForInvalido() {
        String payload = """
                {
                  "email": "email-invalido",
                  "senha": ""
                }
                """;

        requisicao(porta)
                .header(
                        "User-Agent",
                        USER_AGENT
                )
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400)
                .body(
                        "sucesso",
                        equalTo(false)
                )
                .body(
                        "mensagem",
                        equalTo("Dados inválidos")
                );
    }

    @Test
    void deveRecusarAcessoSemToken() {
        requisicao(porta)
                .when()
                .get("/notifications")
                .then()
                .statusCode(401)
                .body(
                        "sucesso",
                        equalTo(false)
                );
    }

    @Test
    void deveAcessarEndpointProtegidoComTokenValido() {
        String token = realizarLoginEObterAccessToken();

        requisicao(porta)
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .when()
                .get("/notifications")
                .then()
                .statusCode(200);
    }

    private String realizarLoginEObterAccessToken() {
        String payload = """
                {
                  "email": "%s",
                  "senha": "%s"
                }
                """.formatted(
                EMAIL,
                SENHA
        );

        Response resposta =
                requisicao(porta)
                        .header(
                                "User-Agent",
                                USER_AGENT
                        )
                        .contentType(
                                "application/json"
                        )
                        .body(payload)
                        .when()
                        .post("/auth/login");

        resposta.then().statusCode(200);

        String token =
                resposta
                        .jsonPath()
                        .getString(
                                "dados.accessToken"
                        );

        if (token == null || token.isBlank()) {
            throw new AssertionError(
                    "Access token não foi retornado pelo login"
            );
        }

        return token;
    }
}