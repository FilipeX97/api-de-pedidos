package br.com.api.pedidos.observability.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability(
        metrics = true,
        tracing = false
)
@ActiveProfiles("test")
class ObservabilidadeActuatorITTest {

    private static final String HEADER_REQUEST_ID =
            "X-Request-Id";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthDeveResponderSemToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(
                        jsonPath("$.components.db.status")
                                .value("UP")
                );
    }

    @Test
    void livenessDeveResponderSemToken() throws Exception {
        mockMvc.perform(
                        get("/actuator/health/liveness")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("UP")
                );
    }

    @Test
    void readinessDeveResponderSemToken() throws Exception {
        mockMvc.perform(
                        get("/actuator/health/readiness")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("UP")
                )
                .andExpect(
                        jsonPath("$.components.db.status")
                                .value("UP")
                );
    }

    @Test
    void infoDeveResponderSemToken() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.app.name")
                                .value("api-de-pedidos")
                )
                .andExpect(
                        jsonPath("$.runtime.java")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.runtime.springBoot")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.runtime.ambiente")
                                .isArray()
                );
    }

    @Test
    void metricsNaoDeveSerPublico() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.sucesso")
                                .value(false)
                );
    }

    @Test
    void prometheusDeveResponderSemToken() throws Exception {
        mockMvc.perform(
                        get("/actuator/prometheus")
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.TEXT_PLAIN
                        )
                )
                .andExpect(
                        content().string(
                                containsString(
                                        "jvm_memory_used_bytes"
                                )
                        )
                );
    }

    @Test
    void deveGerarRequestIdQuandoHeaderNaoForEnviado()
            throws Exception {

        MvcResult resultado = mockMvc.perform(
                        get("/actuator/health")
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().exists(HEADER_REQUEST_ID)
                )
                .andReturn();

        String requestId = resultado
                .getResponse()
                .getHeader(HEADER_REQUEST_ID);

        assertNotNull(requestId);

        assertDoesNotThrow(
                () -> UUID.fromString(requestId)
        );
    }

    @Test
    void devePreservarRequestIdEnviadoPeloCliente()
            throws Exception {

        String requestId = "teste-request-123";

        mockMvc.perform(
                        get("/actuator/health")
                                .header(
                                        HEADER_REQUEST_ID,
                                        requestId
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                HEADER_REQUEST_ID,
                                requestId
                        )
                );
    }
}