package br.com.api.pedidos.order.query;

import br.com.api.pedidos.auth.dto.LoginRequestDTO;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" +
                "jdbc:h2:mem:admin_pedido_security_it;" +
                "DB_CLOSE_DELAY=-1;" +
                "DB_CLOSE_ON_EXIT=FALSE"
})
class AdminPedidoConsultaSecurityITTest {

    private static final String USER_AGENT = "Admin-Pedido-Security-Test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        Mockito.when(
                rateLimitService.permitirRequisicao(
                        Mockito.anyString()
                )
        ).thenReturn(true);
    }

    @Test
    void usuarioComumNaoDeveAcessarConsultaAdministrativa() throws Exception {
        String token = login(
                "user1@teste.com",
                "123456"
        );

        mockMvc.perform(
                        get("/admin/orders")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "User-Agent",
                                        USER_AGENT
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorDeveAcessarConsultaAdministrativa() throws Exception {
        String token = login(
                "admin@api.com",
                "123456"
        );

        mockMvc.perform(
                        get("/admin/orders")
                                .param("page", "0")
                                .param("size", "20")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "User-Agent",
                                        USER_AGENT
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.conteudo").isArray())
                .andExpect(jsonPath("$.dados.paginaAtual").value(0)
                );
    }

    private String login(String email, String senha) throws Exception {
        LoginRequestDTO request =
                new LoginRequestDTO(
                        email,
                        senha
                );

        String response = mockMvc.perform(
                        post("/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .header(
                                        "User-Agent",
                                        USER_AGENT
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json
                .get("dados")
                .get("accessToken")
                .asText();
    }
}
