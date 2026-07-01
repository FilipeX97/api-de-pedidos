package br.com.api.pedidos.auth.integration;

import br.com.api.pedidos.auth.dto.LoginRequestDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerITTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_AGENT = "JUnit-Test-Agent";

    @Test
    void deveRealizarLoginComAdmin() throws Exception {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                "admin@api.com",
                "123456"
        );

        String response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDTO))
                .header("User-Agent", USER_AGENT))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        String accessToken = json.get("dados").get("accessToken").asText();
        String refreshToken = json.get("dados").get("refreshToken").asText();

        assertFalse(accessToken.isBlank());
        assertFalse(refreshToken.isBlank());
    }
}
