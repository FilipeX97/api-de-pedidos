package br.com.api.pedidos.integration.payment;

import br.com.api.pedidos.integration.container.ContainersIntegracao;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.product.repository.ProdutoRepository;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static br.com.api.pedidos.integration.http.RestAssuredIntegracao.requisicao;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
public class PagamentoApiIT extends ContainersIntegracao {

    private static final String USER_AGENT = "api-de-pedidos-integration-test";
    private static final String EMAIL = "pagamento-integracao@api-pedidos.com";
    private static final String SENHA = "SenhaIntegracao123";

    @LocalServerPort
    private int porta;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararUsuario() {
        usuarioRepository.findByEmail(EMAIL).ifPresent(usuarioRepository::delete);
        usuarioRepository.flush();

        Usuario usuario = new Usuario(
                "Usuario Pagamento Integracao",
                EMAIL,
                passwordEncoder.encode(SENHA),
                Perfil.USER
        );

        usuarioRepository.saveAndFlush(usuario);
    }

    @Test
    void deveAprovarPagamentoComCartao() {
        String token = realizarLogin();
        Long produtoId = criarProduto(BigDecimal.valueOf(100), 20);
        Long pedidoId = criarPedido(token);

        adicionarItem(token, pedidoId, produtoId, 2);

        Response resposta = processarPagamento(
                token,
                pedidoId,
                "CARTAO_CREDITO",
                "pagamento-cartao-aprovado-" + System.nanoTime()
        );

        resposta.then()
                .statusCode(201)
                .body("sucesso", equalTo(true))
                .body("dados.idPagamento", notNullValue())
                .body("dados.idPedido", equalTo(pedidoId))
                .body("dados.valor", equalTo(200.0f))
                .body("dados.formaPagamento", equalTo("CARTAO_CREDITO"))
                .body("dados.statusPagamento", equalTo("APROVADO"))
                .body("dados.codigoTransacao", startsWith("CARD-"))
                .body("dados.mensagem", equalTo("Pagamento autorizado pela operadora"))
                .body("mensagem", equalTo("Pagamento processado com sucesso"));

        requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/orders/" + pedidoId + "/payments")
                .then()
                .statusCode(200)
                .body("sucesso", equalTo(true))
                .body("dados.size()", equalTo(1))
                .body("dados[0].statusPagamento", equalTo("APROVADO"));

        consultarPedidoEsperandoStatus(token, pedidoId, "PAGO");
    }

    @Test
    void deveRecusarPagamentoComCartaoAcimaDoLimiteDoGatewayFake() {
        String token = realizarLogin();
        Long produtoId = criarProduto(BigDecimal.valueOf(6000), 10);
        Long pedidoId = criarPedido(token);

        adicionarItem(token, pedidoId, produtoId, 1);

        Response resposta = processarPagamento(
                token,
                pedidoId,
                "CARTAO_CREDITO",
                "pagamento-cartao-recusado-" + System.nanoTime()
        );

        resposta.then()
                .statusCode(201)
                .body("sucesso", equalTo(true))
                .body("dados.idPagamento", notNullValue())
                .body("dados.idPedido", equalTo(pedidoId))
                .body("dados.valor", equalTo(6000.0f))
                .body("dados.formaPagamento", equalTo("CARTAO_CREDITO"))
                .body("dados.statusPagamento", equalTo("RECUSADO"))
                .body("dados.codigoTransacao", startsWith("CARD-"))
                .body("dados.mensagem", equalTo("Pagamento recusado pela operadora"))
                .body("mensagem", equalTo("Pagamento processado com sucesso"));

        consultarPedidoEsperandoStatus(token, pedidoId, "CRIADO");
    }

    @Test
    void deveCriarPixComoPagamentoPendente() {
        String token = realizarLogin();
        Long produtoId = criarProduto(BigDecimal.valueOf(100), 20);
        Long pedidoId = criarPedido(token);

        adicionarItem(token, pedidoId, produtoId, 2);

        Response resposta = processarPagamento(
                token,
                pedidoId,
                "PIX",
                "pagamento-pix-pendente-" + System.nanoTime()
        );

        resposta.then()
                .statusCode(201)
                .body("sucesso", equalTo(true))
                .body("dados.idPagamento", notNullValue())
                .body("dados.idPedido", equalTo(pedidoId))
                .body("dados.valor", equalTo(200.0f))
                .body("dados.formaPagamento", equalTo("PIX"))
                .body("dados.statusPagamento", equalTo("PENDENTE"))
                .body("dados.codigoTransacao", startsWith("PIX-"))
                .body("dados.mensagem", startsWith("PIX gerado."))
                .body("mensagem", equalTo("Pagamento processado com sucesso"));

        consultarPedidoEsperandoStatus(token, pedidoId, "AGUARDANDO_PAGAMENTO");
    }

    @Test
    void deveRetornarMesmaRespostaAoRepetirPagamentoComMesmaIdempotencyKey() {
        String token = realizarLogin();
        Long produtoId = criarProduto(BigDecimal.valueOf(100), 20);
        Long pedidoId = criarPedido(token);

        adicionarItem(token, pedidoId, produtoId, 2);

        String idempotencyKey = "pagamento-idempotente-" + System.nanoTime();

        Response primeiraResposta = processarPagamento(
                token,
                pedidoId,
                "CARTAO_CREDITO",
                idempotencyKey
        );

        primeiraResposta.then()
                .statusCode(201)
                .body("sucesso", equalTo(true))
                .body("dados.statusPagamento", equalTo("APROVADO"));

        String idPagamento = primeiraResposta.jsonPath().getString("dados.idPagamento");
        String codigoTransacao = primeiraResposta.jsonPath().getString("dados.codigoTransacao");

        Response segundaResposta = processarPagamento(
                token,
                pedidoId,
                "CARTAO_CREDITO",
                idempotencyKey
        );

        segundaResposta.then()
                .statusCode(201)
                .body("sucesso", equalTo(true))
                .body("dados.idPagamento", equalTo(idPagamento))
                .body("dados.codigoTransacao", equalTo(codigoTransacao))
                .body("dados.statusPagamento", equalTo("APROVADO"))
                .body("mensagem", equalTo("Requisição já processada anteriormente (idempotência)"));

        requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/orders/" + pedidoId + "/payments")
                .then()
                .statusCode(200)
                .body("sucesso", equalTo(true))
                .body("dados.size()", equalTo(1));
    }

    private String realizarLogin() {
        String payload = """
                {
                  "email": "%s",
                  "senha": "%s"
                }
                """.formatted(EMAIL, SENHA);

        Response resposta = requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/auth/login");

        resposta.then().statusCode(200);

        return resposta.jsonPath().getString("dados.accessToken");
    }

    private Long criarProduto(BigDecimal preco, int estoque) {
        Produto produto = new Produto(
                "Produto Pagamento " + System.nanoTime(),
                "Produto criado para teste de pagamento",
                preco,
                estoque
        );

        return produtoRepository.saveAndFlush(produto).getId();
    }

    private Long criarPedido(String token) {
        Response resposta = requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", "pedido-pagamento-" + System.nanoTime())
                .when()
                .post("/orders");

        resposta.then().statusCode(201);

        return resposta.jsonPath().getLong("dados.idPedido");
    }

    private void adicionarItem(String token, Long pedidoId, Long produtoId, int quantidade) {
        requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", "item-pagamento-" + System.nanoTime())
                .contentType("application/json")
                .body("""
                        {
                          "produtoId": %d,
                          "quantidade": %d
                        }
                        """.formatted(produtoId, quantidade))
                .when()
                .post("/orders/" + pedidoId + "/items")
                .then()
                .statusCode(200);
    }

    private Response processarPagamento(String token, Long pedidoId, String formaPagamento, String idempotencyKey) {
        String payload = """
                {
                  "formaPagamento": "%s"
                }
                """.formatted(formaPagamento);

        return requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", idempotencyKey)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/orders/" + pedidoId + "/payments");
    }

    private void consultarPedidoEsperandoStatus(String token, Long pedidoId, String statusEsperado) {
        requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/orders/" + pedidoId)
                .then()
                .statusCode(200)
                .body("sucesso", equalTo(true))
                .body("dados.status", equalTo(statusEsperado));
    }
}