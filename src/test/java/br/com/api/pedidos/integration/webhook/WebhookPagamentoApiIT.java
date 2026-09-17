package br.com.api.pedidos.integration.webhook;

import br.com.api.pedidos.integration.container.ContainersIntegracao;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.order.service.PedidoService;
import br.com.api.pedidos.payment.adapter.fake.entity.TransacaoGatewayFake;
import br.com.api.pedidos.payment.adapter.fake.repository.TransacaoGatewayFakeRepository;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.repository.PagamentoRepository;
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
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static br.com.api.pedidos.integration.http.RestAssuredIntegracao.requisicao;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
@TestPropertySource(
        properties = "api.security.rate-limit.enabled=false"
)
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
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private TransacaoGatewayFakeRepository transacaoGatewayFakeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AssinaturaWebhookFakeService assinaturaWebhookFakeService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @BeforeEach
    void prepararUsuario() {
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
        Long produtoId = criarProduto(BigDecimal.valueOf(100), 20);
        Long pedidoId = criarPedido(token);

        adicionarItem(token, pedidoId, produtoId, 2);

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
                codigoTransacao,
                "APROVADO"
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
                .body(
                        "dados.idPedido",
                        equalTo(pedidoId.intValue())
                )
                .body("dados.formaPagamento", equalTo("PIX"))
                .body("dados.statusPagamento", equalTo("APROVADO"))
                .body("dados.codigoTransacao", equalTo(codigoTransacao))
                .body("dados.mensagem", equalTo("Pagamento confirmado pelo gateway fake via webhook"))
                .body("mensagem", equalTo("Webhook de pagamento processado com sucesso"));

        consultarPedidoEsperandoStatus(token, pedidoId, "PAGO");

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
        Long produtoId = criarProduto(BigDecimal.valueOf(100), 20);
        Long pedidoId = criarPedido(token);

        adicionarItem(token, pedidoId, produtoId, 2);

        Response respostaPagamento = criarPagamentoPix(token, pedidoId);

        respostaPagamento.then()
                .statusCode(201)
                .body("sucesso", equalTo(true))
                .body("dados.statusPagamento", equalTo("PENDENTE"));

        String codigoTransacao = respostaPagamento.jsonPath().getString("dados.codigoTransacao");
        String eventId = "evt-duplicado-" + System.nanoTime();

        String payload = criarPayloadWebhook(
                eventId,
                codigoTransacao,
                "APROVADO"
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
                "PIX-INEXISTENTE",
                "APROVADO"
        );

        String assinaturaInvalida = "0000000000000000000000000000000000000000000000000000000000000000";

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

    @Test
    void deveReprocessarWebhookAposErro() {
        String token = realizarLogin();
        Long produtoId = criarProduto(BigDecimal.valueOf(6000), 10);
        Long pedidoId = criarPedido(token);

        adicionarItem(token, pedidoId, produtoId, 1);

        Response respostaPagamento = criarPagamentoCartao(token, pedidoId);

        respostaPagamento.then()
                .statusCode(201)
                .body("sucesso", equalTo(true))
                .body("dados.statusPagamento", equalTo("RECUSADO"));

        String codigoTransacao = respostaPagamento.jsonPath().getString("dados.codigoTransacao");

        String eventId = "evt-reprocessamento-" + System.nanoTime();
        String requestId = "request-reprocessamento-" + System.nanoTime();

        String payload = criarPayloadWebhook(eventId, codigoTransacao, "APROVADO");
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
                .statusCode(409)
                .body("sucesso", equalTo(false))
                .body(
                        "mensagem",
                        equalTo(
                                "Somente pagamento pendente pode ser aprovado pelo gateway"
                        )
                );

        WebhookPagamentoRecebido webhookComErro = webhookPagamentoRecebidoRepository
                .findByEventId(eventId)
                .orElseThrow(() -> new AssertionError("Webhook não foi encontrado no PostgreSQL após o erro"));

        assertEquals(StatusProcessamentoWebhook.ERRO, webhookComErro.getStatusProcessamento());
        assertNotNull(webhookComErro.getMensagemErro());

        List<RegistroOperacionalWebhookPagamento> registrosAposErro = registroOperacionalWebhookPagamentoRepository
                .findAll()
                .stream()
                .filter(registro -> eventId.equals(registro.getEventId()))
                .toList();

        assertEquals(1, registrosAposErro.size());

        RegistroOperacionalWebhookPagamento registroAposErro = registrosAposErro.getFirst();

        assertEquals(StatusRegistroOperacionalWebhook.ERRO, registroAposErro.getStatusProcessamento());
        assertFalse(registroAposErro.isDuplicado());
        assertNotNull(registroAposErro.getMensagemErro());
        assertEquals(requestId, registroAposErro.getRequestId());

        colocarPagamentoETransacaoGatewayComoPendentes(
                codigoTransacao
        );

        requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("X-Request-Id", requestId + "-retry")
                .header("X-Fake-Gateway-Signature", assinatura)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/webhooks/payments/fake")
                .then()
                .statusCode(200)
                .body("sucesso", equalTo(true))
                .body(
                        "dados.idPedido",
                        equalTo(pedidoId.intValue())
                )
                .body("dados.formaPagamento", equalTo("CARTAO_CREDITO"))
                .body("dados.statusPagamento", equalTo("APROVADO"))
                .body("dados.codigoTransacao", equalTo(codigoTransacao))
                .body("dados.mensagem", equalTo("Pagamento confirmado pelo gateway fake via webhook"))
                .body("mensagem", equalTo("Webhook de pagamento processado com sucesso"));

        WebhookPagamentoRecebido webhookProcessado = webhookPagamentoRecebidoRepository
                .findByEventId(eventId)
                .orElseThrow(() -> new AssertionError("Webhook não foi encontrado no PostgreSQL após o reprocessamento"));

        assertEquals(StatusProcessamentoWebhook.PROCESSADO, webhookProcessado.getStatusProcessamento());
        assertNull(webhookProcessado.getMensagemErro());

        List<RegistroOperacionalWebhookPagamento> registrosFinais = registroOperacionalWebhookPagamentoRepository
                .findAll()
                .stream()
                .filter(registro -> eventId.equals(registro.getEventId()))
                .toList();

        assertEquals(2, registrosFinais.size());

        long registrosComErro = registrosFinais.stream()
                .filter(registro -> registro.getStatusProcessamento() == StatusRegistroOperacionalWebhook.ERRO)
                .count();

        long registrosProcessados = registrosFinais.stream()
                .filter(registro -> registro.getStatusProcessamento() == StatusRegistroOperacionalWebhook.PROCESSADO)
                .count();

        assertEquals(1, registrosComErro);
        assertEquals(1, registrosProcessados);

        RegistroOperacionalWebhookPagamento registroProcessado = registrosFinais.stream()
                .filter(registro -> registro.getStatusProcessamento() == StatusRegistroOperacionalWebhook.PROCESSADO)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Registro PROCESSADO não encontrado"));

        assertEquals(requestId + "-retry", registroProcessado.getRequestId());
        assertTrue(registroProcessado.isDuplicado());
        assertNull(registroProcessado.getMensagemErro());

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

    private Long criarProduto(BigDecimal preco, int estoque) {
        Produto produto = new Produto(
                "Produto Webhook " + System.nanoTime(),
                "Produto criado para teste de webhook",
                preco,
                estoque
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

    private void adicionarItem(String token, Long pedidoId, Long idProduto, int quantidade) {
        requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", "item-webhook-" + System.nanoTime())
                .contentType("application/json")
                .body("""
                        {
                          "idProduto": %d,
                          "quantidade": %d
                        }
                        """.formatted(idProduto, quantidade))
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

    private String criarPayloadWebhook(String eventId, String codigoTransacao, String statusPagamento) {
        return """
                {
                  "eventId": "%s",
                  "tipo": "PAYMENT_UPDATED",
                  "codigoTransacao": "%s",
                  "statusPagamento": "%s",
                  "dataEvento": "2026-09-01T18:30:00Z"
                }
                """.formatted(eventId, codigoTransacao, statusPagamento);
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

    private Response criarPagamentoCartao(String token, Long pedidoId) {
        return requisicao(porta)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", "pagamento-webhook-cartao-" + System.nanoTime())
                .contentType("application/json")
                .body("""
                    {
                      "formaPagamento": "CARTAO_CREDITO"
                    }
                    """)
                .when()
                .post("/orders/" + pedidoId + "/payments");
    }

    private void colocarPagamentoETransacaoGatewayComoPendentes(
            String codigoTransacao
    ) {
        Pagamento pagamento = pagamentoRepository
                .findByCodigoTransacao(codigoTransacao)
                .orElseThrow(() ->
                        new AssertionError(
                                "Pagamento não encontrado para preparar o reprocessamento"
                        )
                );

        Long idPedido = pagamento.getPedido().getId();

        pagamento.deixarPendente(
                codigoTransacao,
                "Pagamento aguardando confirmação para reprocessamento"
        );

        pagamentoRepository.saveAndFlush(pagamento);

        TransacaoGatewayFake transacao = transacaoGatewayFakeRepository
                .findByCodigoTransacao(codigoTransacao)
                .orElseThrow(() ->
                        new AssertionError(
                                "Transação não encontrada no gateway fake"
                        )
                );

        transacao.atualizarStatus(StatusPagamento.PENDENTE);
        transacaoGatewayFakeRepository.saveAndFlush(transacao);

        Pedido pedido = pedidoRepository
                .findByIdComItens(idPedido)
                .orElseThrow(() ->
                        new AssertionError(
                                "Pedido não encontrado para preparar o reprocessamento"
                        )
                );

        pedidoService.marcarPedidoComoAguardandoPagamento(pedido);
        pedidoRepository.saveAndFlush(pedido);
    }
}