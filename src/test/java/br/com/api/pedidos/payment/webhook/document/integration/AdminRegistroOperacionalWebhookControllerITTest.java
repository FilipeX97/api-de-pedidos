package br.com.api.pedidos.payment.webhook.document.integration;

import br.com.api.pedidos.auth.dto.LoginRequestDTO;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.webhook.document.dto.RegistroOperacionalWebhookPagamentoFiltroDTO;
import br.com.api.pedidos.payment.webhook.document.dto.RegistroOperacionalWebhookPagamentoResponseDTO;
import br.com.api.pedidos.payment.webhook.document.entity.StatusRegistroOperacionalWebhook;
import br.com.api.pedidos.payment.webhook.document.service.RegistroOperacionalWebhookPagamentoConsultaService;
import br.com.api.pedidos.security.ratelimit.RateLimitService;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "management.health.mongo.enabled=false",
        "spring.data.mongodb.auto-index-creation=false",
        "spring.data.mongodb.uri=" +
                "mongodb://localhost:27017/" +
                "api_pedidos_operacional_controller_test" +
                "?serverSelectionTimeoutMS=200"
})
class AdminRegistroOperacionalWebhookControllerITTest {

    private static final String ENDPOINT =
            "/admin/webhooks/payments/operational";

    private static final String USER_AGENT =
            "Admin-Webhook-Operational-Test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegistroOperacionalWebhookPagamentoConsultaService
            consultaService;

    @MockitoBean
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        Mockito.reset(
                consultaService,
                rateLimitService
        );

        when(
                rateLimitService.permitirRequisicao(
                        any(String.class)
                )
        ).thenReturn(true);
    }

    @Test
    void semTokenNaoDeveAcessarConsulta() throws Exception {
        mockMvc.perform(
                        get(ENDPOINT)
                                .header(
                                        "User-Agent",
                                        USER_AGENT
                                )
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(consultaService);
    }

    @Test
    void usuarioComumNaoDeveAcessarConsulta() throws Exception {
        String tokenUser = login(
                "user1@teste.com",
                "123456"
        );

        mockMvc.perform(
                        get(ENDPOINT)
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenUser
                                )
                                .header(
                                        "User-Agent",
                                        USER_AGENT
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.sucesso")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.mensagem")
                                .value("Acesso negado")
                );

        verifyNoInteractions(consultaService);
    }

    @Test
    void administradorDeveConsultarComFiltros() throws Exception {
        String tokenAdmin = login(
                "admin@api.com",
                "123456"
        );

        when(
                consultaService.consultar(
                        any(
                                RegistroOperacionalWebhookPagamentoFiltroDTO
                                        .class
                        ),
                        any(Pageable.class)
                )
        ).thenReturn(paginaComRegistro());

        mockMvc.perform(
                        get(ENDPOINT)
                                .param(
                                        "eventId",
                                        "evt-123"
                                )
                                .param(
                                        "codigoTransacao",
                                        "PIX-123"
                                )
                                .param(
                                        "statusProcessamento",
                                        "PROCESSADO"
                                )
                                .param(
                                        "dataInicio",
                                        "2026-07-28T00:00:00Z"
                                )
                                .param(
                                        "dataFim",
                                        "2026-07-28T23:59:59Z"
                                )
                                .param(
                                        "duplicado",
                                        "true"
                                )
                                .param("page", "0")
                                .param("size", "20")
                                .param(
                                        "sort",
                                        "dataRecebimento,desc"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAdmin
                                )
                                .header(
                                        "User-Agent",
                                        USER_AGENT
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.sucesso")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Registros operacionais "
                                                + "de webhooks encontrados"
                                )
                )
                .andExpect(
                        jsonPath("$.dados.conteudo")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.dados.conteudo[0].id")
                                .value("mongo-123")
                )
                .andExpect(
                        jsonPath(
                                "$.dados.conteudo[0].eventId"
                        ).value("evt-123")
                )
                .andExpect(
                        jsonPath(
                                "$.dados.conteudo[0]"
                                        + ".codigoTransacao"
                        ).value("PIX-123")
                )
                .andExpect(
                        jsonPath(
                                "$.dados.conteudo[0]"
                                        + ".statusProcessamento"
                        ).value("PROCESSADO")
                )
                .andExpect(
                        jsonPath(
                                "$.dados.conteudo[0].duplicado"
                        ).value(true)
                )
                .andExpect(
                        jsonPath("$.dados.paginaAtual")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.dados.totalElementos")
                                .value(1)
                );

        ArgumentCaptor<
                RegistroOperacionalWebhookPagamentoFiltroDTO
                > filtroCaptor =
                ArgumentCaptor.forClass(
                        RegistroOperacionalWebhookPagamentoFiltroDTO
                                .class
                );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(consultaService).consultar(
                filtroCaptor.capture(),
                pageableCaptor.capture()
        );

        RegistroOperacionalWebhookPagamentoFiltroDTO filtro =
                filtroCaptor.getValue();

        assertEquals(
                "evt-123",
                filtro.eventId()
        );

        assertEquals(
                "PIX-123",
                filtro.codigoTransacao()
        );

        assertEquals(
                StatusRegistroOperacionalWebhook.PROCESSADO,
                filtro.statusProcessamento()
        );

        assertEquals(
                Instant.parse(
                        "2026-07-28T00:00:00Z"
                ),
                filtro.dataInicio()
        );

        assertEquals(
                Instant.parse(
                        "2026-07-28T23:59:59Z"
                ),
                filtro.dataFim()
        );

        assertEquals(true, filtro.duplicado());

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());

        Sort.Order ordem =
                pageable.getSort()
                        .getOrderFor("dataRecebimento");

        assertNotNull(ordem);
        assertEquals(
                Sort.Direction.DESC,
                ordem.getDirection()
        );
    }

    @Test
    void statusOperacionalInvalidoDeveRetornarBadRequest()
            throws Exception {

        String tokenAdmin = login(
                "admin@api.com",
                "123456"
        );

        mockMvc.perform(
                        get(ENDPOINT)
                                .param(
                                        "statusProcessamento",
                                        "STATUS_INEXISTENTE"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAdmin
                                )
                                .header(
                                        "User-Agent",
                                        USER_AGENT
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(consultaService);
    }

    @Test
    void falhaNaConsultaMongoDeveRetornarErroInterno()
            throws Exception {

        String tokenAdmin = login(
                "admin@api.com",
                "123456"
        );

        when(
                consultaService.consultar(
                        any(
                                RegistroOperacionalWebhookPagamentoFiltroDTO
                                        .class
                        ),
                        any(Pageable.class)
                )
        ).thenThrow(
                new RuntimeException(
                        "MongoDB indisponível"
                )
        );

        mockMvc.perform(
                        get(ENDPOINT)
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenAdmin
                                )
                                .header(
                                        "User-Agent",
                                        USER_AGENT
                                )
                )
                .andExpect(status().isInternalServerError())
                .andExpect(
                        jsonPath("$.sucesso")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.mensagem")
                                .value(
                                        "Erro interno inesperado"
                                )
                );
    }

    private PaginaResponseDTO<
            RegistroOperacionalWebhookPagamentoResponseDTO
            > paginaComRegistro() {

        RegistroOperacionalWebhookPagamentoResponseDTO registro =
                new RegistroOperacionalWebhookPagamentoResponseDTO(
                        "mongo-123",
                        "evt-123",
                        "PIX-123",
                        StatusPagamento.APROVADO,
                        StatusRegistroOperacionalWebhook.PROCESSADO,
                        "{\"eventId\":\"evt-123\"}",
                        "request-123",
                        "PAYMENT_UPDATED",
                        "FAKE_GATEWAY",
                        Instant.parse(
                                "2026-07-28T20:30:00Z"
                        ),
                        Instant.parse(
                                "2026-07-28T20:30:01Z"
                        ),
                        180L,
                        true,
                        null
                );

        return new PaginaResponseDTO<>(
                List.of(registro),
                0,
                1,
                1L,
                20,
                1,
                true,
                true,
                false
        );
    }

    private String login(
            String email,
            String senha
    ) throws Exception {

        LoginRequestDTO request =
                new LoginRequestDTO(
                        email,
                        senha
                );

        String response =
                mockMvc.perform(
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

        JsonNode json =
                objectMapper.readTree(response);

        return json
                .get("dados")
                .get("accessToken")
                .asText();
    }
}