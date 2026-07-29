package br.com.api.pedidos.order.integration;

import br.com.api.pedidos.auth.dto.LoginRequestDTO;
import br.com.api.pedidos.order.dto.AdicionarPedidoRequestDTO;
import br.com.api.pedidos.payment.dto.PagamentoRequestDTO;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.product.repository.ProdutoRepository;
import br.com.api.pedidos.security.ratelimit.RateLimitService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PedidoControllerITTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProdutoRepository produtoRepository;

    @MockitoBean
    private RateLimitService rateLimitService;

    private static final String USER_AGENT = "JUnit-Test-Agent";

    @BeforeEach
    void setUp() {
        Mockito.when(rateLimitService.permitirRequisicao(Mockito.anyString()))
                .thenReturn(true);
    }

    @Test
    void usuarioDeveCriarPedidoAdicionarItemEPagar() throws Exception {
        String tokenUser = login("user1@teste.com", "123456");

        Long produtoId = criarProdutoParaTeste(20);
        Long pedidoId = criarPedido(tokenUser);
        adicionarItem(tokenUser, pedidoId, produtoId, 2);

        PagamentoRequestDTO pagamentoRequestDTO =
                new PagamentoRequestDTO(FormaPagamento.CARTAO_CREDITO);

        mockMvc.perform(post("/orders/" + pedidoId + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenUser)
                        .header("User-Agent", USER_AGENT)
                        .header("Idempotency-Key", "pay-it-" + System.nanoTime())
                        .content(objectMapper.writeValueAsString(pagamentoRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.statusPagamento").value("APROVADO"));
    }

    @Test
    void usuarioComumNaoDeveEnviarPedido() throws Exception {
        String tokenUser = login("user1@teste.com", "123456");

        Long produtoId = criarProdutoParaTeste(20);
        Long pedidoId = criarPedido(tokenUser);
        adicionarItem(tokenUser, pedidoId, produtoId, 1);

        mockMvc.perform(post("/orders/" + pedidoId + "/ship")
                .header("Authorization",  "Bearer " + tokenUser)
                .header("User-Agent", USER_AGENT)
                .header("Idempotency-Key", "ship-user-it-" + System.nanoTime()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornarMesmaRespostaAoPagarPedidoDuasVezesComMesmaIdempotencyKey() throws Exception {
        String tokenUser = login("user1@teste.com", "123456");

        Long produtoId = criarProdutoParaTeste(20);
        Long pedidoId = criarPedido(tokenUser);
        adicionarItem(tokenUser, pedidoId, produtoId, 2);

        String idempotencyKey = "pay-repeat-" + System.nanoTime();

        PagamentoRequestDTO pagamentoRequestDTO =
                new PagamentoRequestDTO(FormaPagamento.CARTAO_CREDITO);

        mockMvc.perform(post("/orders/" + pedidoId + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenUser)
                        .header("User-Agent", USER_AGENT)
                        .header("Idempotency-Key", idempotencyKey)
                        .content(objectMapper.writeValueAsString(pagamentoRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.statusPagamento").value("APROVADO"));

        mockMvc.perform(post("/orders/" + pedidoId + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenUser)
                        .header("User-Agent", USER_AGENT)
                        .header("Idempotency-Key", idempotencyKey)
                        .content(objectMapper.writeValueAsString(pagamentoRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.statusPagamento").value("APROVADO"))
                .andExpect(jsonPath("$.mensagem").value(
                        "Requisição já processada anteriormente (idempotência)"
                ));
    }

    private Long criarPedido(String token) throws Exception {
        String response = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("User-Agent", USER_AGENT)
                        .header("Idempotency-Key", "pedido-it-" + System.nanoTime()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("dados").get("idPedido").asLong();
    }

    private void adicionarItem(
            String token,
            Long pedidoId,
            Long produtoId,
            Integer quantidade
    ) throws Exception {
        AdicionarPedidoRequestDTO request =
                new AdicionarPedidoRequestDTO(produtoId, quantidade);

        mockMvc.perform(post("/orders/" + pedidoId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .header("User-Agent", USER_AGENT)
                        .header("Idempotency-Key", "item-it-" + System.nanoTime())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private String login(String email, String senha) throws Exception {
        LoginRequestDTO request = new LoginRequestDTO(email, senha);

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", USER_AGENT)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("dados").get("accessToken").asText();
    }

    private Long criarProdutoParaTeste(Integer estoque) {
        Produto produto = produtoRepository.save(
                new Produto(
                        "Produto Pedido IT " + System.nanoTime(),
                        "Produto usado no teste de pedido",
                        BigDecimal.valueOf(100),
                        estoque
                )
        );

        return produto.getId();
    }

}
