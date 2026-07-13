package br.com.api.pedidos.product.integration;

import br.com.api.pedidos.auth.dto.LoginRequestDTO;
import br.com.api.pedidos.product.dto.ProdutoRequestDTO;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProdutoControllerITTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RateLimitService rateLimitService;

    private static final String USER_AGENT = "JUnit-Test-Agent";

    @BeforeEach
    void setUp() {
        Mockito.when(rateLimitService.permitirRequisicao(Mockito.anyString()))
                .thenReturn(true);
    }

    @Test
    void adminDeveCriarProduto() throws Exception {
        String tokenAdmin = login("admin@api.com", "123456");
        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "Produto Teste",
                "Descrição do produto teste",
                BigDecimal.valueOf(100),
                10
        );

        mockMvc.perform(post("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("User-Agent", USER_AGENT)
                .header("Idempotency-Key", "produto-teste-" + System.nanoTime())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void usuarioComumNaoDeveCriarProduto() throws Exception {
        String tokenUser = login("user1@teste.com", "123456");
        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "Produto user",
                "Não deve criar",
                BigDecimal.valueOf(100),
                10
        );

        mockMvc.perform(post("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tokenUser)
                .header("User-Agent", USER_AGENT)
                .header("Idempotency-Key", "produto-user-teste-" + System.nanoTime())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminNaoDeveCriarProdutoComDadosInvalidos() throws Exception {
        String tokenAdmin = login("admin@api.com", "123456");

        String payload = """
            {
              "nome": "Produto inválido",
              "descricao": "Produto com preço inválido",
              "preco": -10,
              "estoque": -1
            }
            """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .header("User-Agent", USER_AGENT)
                        .header("Idempotency-Key", "produto-invalido-" + System.nanoTime())
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
                .andExpect(jsonPath("$.dados.preco").exists())
                .andExpect(jsonPath("$.dados.estoque").exists());
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
}
