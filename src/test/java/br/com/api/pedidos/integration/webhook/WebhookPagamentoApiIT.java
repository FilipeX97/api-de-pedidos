package br.com.api.pedidos.integration.webhook;

import br.com.api.pedidos.integration.container.ContainersIntegracao;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.webhook.document.entity.RegistroOperacionalWebhookPagamento;
import br.com.api.pedidos.payment.webhook.document.entity.StatusRegistroOperacionalWebhook;
import br.com.api.pedidos.payment.webhook.document.repository.RegistroOperacionalWebhookPagamentoRepository;
import br.com.api.pedidos.payment.webhook.entity.StatusProcessamentoWebhook;
import br.com.api.pedidos.payment.webhook.entity.WebhookPagamentoRecebido;
import br.com.api.pedidos.payment.webhook.repository.WebhookPagamentoRecebidoRepository;
import br.com.api.pedidos.payment.webhook.service.AssinaturaWebhookFakeService;
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
import java.util.List;

import static br.com.api.pedidos.integration.http.RestAssuredIntegracao.requisicao;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
public class WebhookPagamentoApiIT extends ContainersIntegracao {

    private static final String USER_AGENT = "api-de-pedidos-integration-test";
    private static final String EMAIL = "webhook-integracao@api-pedidos.com";
    private static final String SENHA = "SenhaIntegracao123";

    @LocalServerPort
    private int porta;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private WebhookPagamentoRecebidoRepository webhookPagamentoRecebidoRepository;

    @Autowired
    private RegistroOperacionalWebhookPagamentoRepository registroOperacionalWebhookPagamentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AssinaturaWebhookFakeService assinaturaWebhookFakeService;

    @BeforeEach
    void prepararUsuario() {
        usuarioRepository.findByEmail(EMAIL).ifPresent(usuarioRepository::delete);
        usuarioRepository.flush();

        Usuario usuario = new Usuario(
                "Usuario Webhook Integracao",
                EMAIL,
                passwordEncoder.encode(SENHA),
                Perfil.USER
        );

        usuarioRepository.saveAndFlush(usuario);
    }

    @Test
    void deveProcessarWebhookDeAprovacaoEAtualizarPostgreSQLEMongoDB() {
        String token = realizarLogin();
        Long produtoId = criarProduto(BigDecimal.valueOf(100));
        Long pedidoId = criarPedido(token);

        adicionarItem(token, pedidoId, produtoId);

        Response respostaPagamento = criarPagamentoPix(token, pedidoId);

        respostaPagamento.then()
                .statusCode(201)
                .body("sucesso", equalTo(true))
                .body("dados.statusPagamento", equalTo("PENDENTE"));

        String codigoTransacao = respostaPagamento.jsonPath().getString("dados.codigoTransacao");

        String eventId = "evt-aprovacao-" + System.nanoTime();
        String requestId = "webhook-request-" + System.nanoTime();

        String payload = criarPayloadWebhook(
                eventId,
                codigoTransacao
        );

        String assinatura = assinaturaWebhookFakeService.gerarAssinatura(payload);

        requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("X-Request-Id", requestId)
                .header("X-Fake-Gateway-Signature", assinatura)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/webhooks/payments/fake")
                .then()
                .statusCode(200)
                .body("sucesso", equalTo(true))
                .body("dados.idPedido", equalTo(pedidoId))
                .body("dados.formaPagamento", equalTo("PIX"))
                .body("dados.statusPagamento", equalTo("APROVADO"))
                .body("dados.codigoTransacao", equalTo(codigoTransacao))
                .body("dados.mensagem", equalTo("Pagamento confirmado pelo gateway fake via webhook"))
                .body("mensagem", equalTo("Webhook de pagamento processado com sucesso"));

        consultarPedidoEsperandoStatus(token, pedidoId);

        WebhookPagamentoRecebido webhook = webhookPagamentoRecebidoRepository
                .findByEventId(eventId)
                .orElseThrow(() -> new AssertionError("Webhook não foi persistido no PostgreSQL"));

        assertEquals(codigoTransacao, webhook.getCodigoTransacao());
        assertEquals(StatusPagamento.APROVADO, webhook.getStatusRecebido());
        assertEquals(StatusProcessamentoWebhook.PROCESSADO, webhook.getStatusProcessamento());

        List<RegistroOperacionalWebhookPagamento> registros = registroOperacionalWebhookPagamentoRepository
                .findAll()
                .stream()
                .filter(registro -> eventId.equals(registro.getEventId()))
                .toList();

        assertEquals(1, registros.size());

        RegistroOperacionalWebhookPagamento registro = registros.getFirst();

        assertEquals(eventId, registro.getEventId());
        assertEquals(codigoTransacao, registro.getCodigoTransacao());
        assertEquals(StatusPagamento.APROVADO, registro.getStatusRecebido());
        assertEquals(StatusRegistroOperacionalWebhook.PROCESSADO, registro.getStatusProcessamento());
        assertEquals(payload, registro.getPayloadOriginal());
        assertEquals(requestId, registro.getRequestId());
        assertEquals("PAYMENT_UPDATED", registro.getTipoEvento());
        assertEquals("FAKE_GATEWAY", registro.getOrigem());
        assertFalse(registro.isDuplicado());
        assertNotNull(registro.getDataRecebimento());
        assertNotNull(registro.getDataProcessamento());
        assertNotNull(registro.getDuracaoProcessamentoMs());
    }

    @Test
    void deveIgnorarWebhookDuplicadoJaProcessado() {
        String token = realizarLogin();
        Long produtoId = criarProduto(BigDecimal.valueOf(100));
        Long pedidoId = criarPedido(token);

        adicionarItem(token, pedidoId, produtoId);

        Response respostaPagamento = criarPagamentoPix(token, pedidoId);

        respostaPagamento.then()
                .statusCode(201)
                .body("sucesso", equalTo(true))
                .body("dados.statusPagamento", equalTo("PENDENTE"));

        String codigoTransacao = respostaPagamento.jsonPath().getString("dados.codigoTransacao");
        String eventId = "evt-duplicado-" + System.nanoTime();

        String payload = criarPayloadWebhook(
                eventId,
                codigoTransacao
        );

        String assinatura = assinaturaWebhookFakeService.gerarAssinatura(payload);

        requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("X-Fake-Gateway-Signature", assinatura)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/webhooks/payments/fake")
                .then()
                .statusCode(200)
                .body("sucesso", equalTo(true))
                .body("dados.statusPagamento", equalTo("APROVADO"));

        requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("X-Fake-Gateway-Signature", assinatura)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/webhooks/payments/fake")
                .then()
                .statusCode(200)
                .body("sucesso", equalTo(true))
                .body("dados.statusPagamento", equalTo("APROVADO"))
                .body("mensagem", equalTo("Webhook de pagamento processado com sucesso"));

        WebhookPagamentoRecebido webhook = webhookPagamentoRecebidoRepository
                .findByEventId(eventId)
                .orElseThrow(() -> new AssertionError("Webhook não foi encontrado no PostgreSQL"));

        assertEquals(StatusProcessamentoWebhook.PROCESSADO, webhook.getStatusProcessamento());

        List<RegistroOperacionalWebhookPagamento> registros = registroOperacionalWebhookPagamentoRepository
                .findAll()
                .stream()
                .filter(registro -> eventId.equals(registro.getEventId()))
                .toList();

        assertEquals(2, registros.size());

        RegistroOperacionalWebhookPagamento primeiroRegistro = registros.stream()
                .filter(registro -> registro.getStatusProcessamento() == StatusRegistroOperacionalWebhook.PROCESSADO)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Registro PROCESSADO não encontrado"));

        RegistroOperacionalWebhookPagamento segundoRegistro = registros.stream()
                .filter(registro -> registro.getStatusProcessamento() == StatusRegistroOperacionalWebhook.DUPLICADO)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Registro DUPLICADO não encontrado"));

        assertEquals(eventId, primeiroRegistro.getEventId());
        assertEquals(eventId, segundoRegistro.getEventId());

        assertFalse(primeiroRegistro.isDuplicado());
        assertTrue(segundoRegistro.isDuplicado());

        assertEquals(StatusRegistroOperacionalWebhook.PROCESSADO, primeiroRegistro.getStatusProcessamento());
        assertEquals(StatusRegistroOperacionalWebhook.DUPLICADO, segundoRegistro.getStatusProcessamento());

        assertNotNull(primeiroRegistro.getDataProcessamento());
        assertNotNull(segundoRegistro.getDataProcessamento());
        assertNotNull(primeiroRegistro.getDuracaoProcessamentoMs());
        assertNotNull(segundoRegistro.getDuracaoProcessamentoMs());

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
    }

    @Test
    void deveRejeitarWebhookComAssinaturaInvalida() {
        String eventId = "evt-assinatura-invalida-" + System.nanoTime();

        String payload = criarPayloadWebhook(
                eventId,
                "PIX-INEXISTENTE"
        );

        String assinaturaInvalida =
                "0000000000000000000000000000000000000000000000000000000000000000";

        requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("X-Fake-Gateway-Signature", assinaturaInvalida)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/webhooks/payments/fake")
                .then()
                .statusCode(401)
                .body("sucesso", equalTo(false))
                .body("mensagem", equalTo("Assinatura do webhook inválida"));

        List<RegistroOperacionalWebhookPagamento> registros = registroOperacionalWebhookPagamentoRepository
                .findAll()
                .stream()
                .filter(registro -> eventId.equals(registro.getEventId()))
                .toList();

        assertTrue(registros.isEmpty());
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

        String token = resposta.jsonPath().getString("dados.accessToken");

        assertNotNull(token);
        assertFalse(token.isBlank());

        return token;
    }

    private Long criarProduto(BigDecimal preco) {
        Produto produto = new Produto(
                "Produto Webhook " + System.nanoTime(),
                "Produto criado para teste de webhook",
                preco,
                20
        );

        return produtoRepository.saveAndFlush(produto).getId();
    }

    private Long criarPedido(String token) {
        Response resposta = requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", "pedido-webhook-" + System.nanoTime())
                .when()
                .post("/orders");

        resposta.then().statusCode(201);

        return resposta.jsonPath().getLong("dados.idPedido");
    }

    private void adicionarItem(String token, Long pedidoId, Long produtoId) {
        requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", "item-webhook-" + System.nanoTime())
                .contentType("application/json")
                .body("""
                        {
                          "produtoId": %d,
                          "quantidade": %d
                        }
                        """.formatted(produtoId, 2))
                .when()
                .post("/orders/" + pedidoId + "/items")
                .then()
                .statusCode(200);
    }

    private Response criarPagamentoPix(String token, Long pedidoId) {
        return requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", "pagamento-webhook-" + System.nanoTime())
                .contentType("application/json")
                .body("""
                        {
                          "formaPagamento": "PIX"
                        }
                        """)
                .when()
                .post("/orders/" + pedidoId + "/payments");
    }

    private String criarPayloadWebhook(String eventId, String codigoTransacao) {
        return """
                {
                  "eventId": "%s",
                  "tipo": "PAYMENT_UPDATED",
                  "codigoTransacao": "%s",
                  "statusPagamento": "%s",
                  "dataEvento": "2026-09-01T18:30:00Z"
                }
                """.formatted(eventId, codigoTransacao, "APROVADO");
    }

    private void consultarPedidoEsperandoStatus(String token, Long pedidoId) {
        requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/orders/" + pedidoId)
                .then()
                .statusCode(200)
                .body("sucesso", equalTo(true))
                .body("dados.status", equalTo("PAGO"));
    }
}