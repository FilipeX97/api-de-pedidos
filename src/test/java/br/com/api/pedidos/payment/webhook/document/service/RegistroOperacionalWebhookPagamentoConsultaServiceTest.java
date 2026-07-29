package br.com.api.pedidos.payment.webhook.document.service;

import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.webhook.document.dto.RegistroOperacionalWebhookPagamentoFiltroDTO;
import br.com.api.pedidos.payment.webhook.document.dto.RegistroOperacionalWebhookPagamentoResponseDTO;
import br.com.api.pedidos.payment.webhook.document.entity.RegistroOperacionalWebhookPagamento;
import br.com.api.pedidos.payment.webhook.document.entity.StatusRegistroOperacionalWebhook;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroOperacionalWebhookPagamentoConsultaServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private RegistroOperacionalWebhookPagamentoConsultaService service;

    @Test
    void deveConsultarComTodosOsFiltrosEPaginacao() {
        Instant dataInicio =
                Instant.parse("2026-07-28T00:00:00Z");

        Instant dataFim =
                Instant.parse("2026-07-28T23:59:59Z");

        RegistroOperacionalWebhookPagamentoFiltroDTO filtro =
                new RegistroOperacionalWebhookPagamentoFiltroDTO(
                        " evt-123 ",
                        " PIX-123 ",
                        StatusRegistroOperacionalWebhook.PROCESSADO,
                        dataInicio,
                        dataFim,
                        true
                );

        Pageable pageable =
                PageRequest.of(
                        1,
                        10,
                        Sort.by(
                                Sort.Direction.ASC,
                                "eventId"
                        )
                );

        List<RegistroOperacionalWebhookPagamento> documentos =
                List.of(
                        criarRegistro(
                                "mongo-1",
                                "evt-123",
                                true
                        ),
                        criarRegistro(
                                "mongo-2",
                                "evt-123",
                                true
                        )
                );

        when(mongoTemplate.count(
                any(Query.class),
                eq(RegistroOperacionalWebhookPagamento.class)
        )).thenReturn(32L);

        when(mongoTemplate.find(
                any(Query.class),
                eq(RegistroOperacionalWebhookPagamento.class)
        )).thenReturn(documentos);

        PaginaResponseDTO<
                RegistroOperacionalWebhookPagamentoResponseDTO
                > resultado =
                service.consultar(
                        filtro,
                        pageable
                );

        assertEquals(1, resultado.paginaAtual());
        assertEquals(4, resultado.totalPaginas());
        assertEquals(32L, resultado.totalElementos());
        assertEquals(10, resultado.tamanhoPagina());
        assertEquals(2, resultado.quantidadeElementos());
        assertEquals(2, resultado.conteudo().size());
        assertEquals(
                "mongo-1",
                resultado.conteudo().get(0).id()
        );

        ArgumentCaptor<Query> queryCaptor =
                ArgumentCaptor.forClass(Query.class);

        verify(mongoTemplate).find(
                queryCaptor.capture(),
                eq(RegistroOperacionalWebhookPagamento.class)
        );

        Query query = queryCaptor.getValue();

        Document filtros = query.getQueryObject();

        assertEquals(
                "evt-123",
                filtros.get("eventId")
        );

        assertEquals(
                "PIX-123",
                filtros.get("codigoTransacao")
        );

        assertEquals(
                StatusRegistroOperacionalWebhook.PROCESSADO,
                filtros.get("statusProcessamento")
        );

        assertEquals(
                true,
                filtros.get("duplicado")
        );

        Document periodo =
                (Document) filtros.get("dataRecebimento");

        assertEquals(
                dataInicio,
                periodo.get("$gte")
        );

        assertEquals(
                dataFim,
                periodo.get("$lte")
        );

        assertEquals(10L, query.getSkip());
        assertEquals(10, query.getLimit());

        assertEquals(
                1,
                query.getSortObject().get("eventId")
        );
    }

    @Test
    void deveUsarOrdenacaoPadraoQuandoNaoForInformada() {
        RegistroOperacionalWebhookPagamentoFiltroDTO filtro =
                filtroVazio();

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.unsorted()
                );

        when(mongoTemplate.count(
                any(Query.class),
                eq(RegistroOperacionalWebhookPagamento.class)
        )).thenReturn(0L);

        when(mongoTemplate.find(
                any(Query.class),
                eq(RegistroOperacionalWebhookPagamento.class)
        )).thenReturn(List.of());

        PaginaResponseDTO<
                RegistroOperacionalWebhookPagamentoResponseDTO
                > resultado =
                service.consultar(
                        filtro,
                        pageable
                );

        assertTrue(resultado.vazia());
        assertEquals(0L, resultado.totalElementos());

        ArgumentCaptor<Query> queryCaptor =
                ArgumentCaptor.forClass(Query.class);

        verify(mongoTemplate).find(
                queryCaptor.capture(),
                eq(RegistroOperacionalWebhookPagamento.class)
        );

        Query query = queryCaptor.getValue();

        assertTrue(query.getQueryObject().isEmpty());

        assertEquals(
                -1,
                query.getSortObject().get("dataRecebimento")
        );
    }

    @Test
    void deveFiltrarSomentePorDataInicial() {
        Instant dataInicio =
                Instant.parse("2026-07-28T00:00:00Z");

        RegistroOperacionalWebhookPagamentoFiltroDTO filtro =
                new RegistroOperacionalWebhookPagamentoFiltroDTO(
                        null,
                        null,
                        null,
                        dataInicio,
                        null,
                        null
                );

        when(mongoTemplate.count(
                any(Query.class),
                eq(RegistroOperacionalWebhookPagamento.class)
        )).thenReturn(0L);

        when(mongoTemplate.find(
                any(Query.class),
                eq(RegistroOperacionalWebhookPagamento.class)
        )).thenReturn(List.of());

        service.consultar(
                filtro,
                PageRequest.of(0, 20)
        );

        ArgumentCaptor<Query> queryCaptor =
                ArgumentCaptor.forClass(Query.class);

        verify(mongoTemplate).find(
                queryCaptor.capture(),
                eq(RegistroOperacionalWebhookPagamento.class)
        );

        Document periodo =
                (Document) queryCaptor
                        .getValue()
                        .getQueryObject()
                        .get("dataRecebimento");

        assertEquals(
                dataInicio,
                periodo.get("$gte")
        );

        assertEquals(1, periodo.size());
    }

    @Test
    void naoDeveConsultarComPeriodoInvertido() {
        RegistroOperacionalWebhookPagamentoFiltroDTO filtro =
                new RegistroOperacionalWebhookPagamentoFiltroDTO(
                        null,
                        null,
                        null,
                        Instant.parse(
                                "2026-07-29T00:00:00Z"
                        ),
                        Instant.parse(
                                "2026-07-28T00:00:00Z"
                        ),
                        null
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.consultar(
                                filtro,
                                PageRequest.of(0, 20)
                        )
                );

        assertEquals(
                "Data inicial não pode ser posterior à data final",
                exception.getMessage()
        );

        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void naoDeveAceitarPaginaAcimaDoLimite() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.consultar(
                                filtroVazio(),
                                PageRequest.of(0, 101)
                        )
                );

        assertEquals(
                "Tamanho máximo da página é 100",
                exception.getMessage()
        );

        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void naoDeveAceitarOrdenacaoPorPayload() {
        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by("payloadOriginal")
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.consultar(
                                filtroVazio(),
                                pageable
                        )
                );

        assertTrue(
                exception.getMessage().contains(
                        "Campo de ordenação inválido"
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "payloadOriginal"
                )
        );

        verifyNoInteractions(mongoTemplate);
    }

    private RegistroOperacionalWebhookPagamentoFiltroDTO
    filtroVazio() {
        return new RegistroOperacionalWebhookPagamentoFiltroDTO(
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private RegistroOperacionalWebhookPagamento criarRegistro(
            String id,
            String eventId,
            boolean duplicado
    ) {
        RegistroOperacionalWebhookPagamento registro =
                new RegistroOperacionalWebhookPagamento(
                        eventId,
                        "PIX-123",
                        StatusPagamento.APROVADO,
                        "{}",
                        "request-123",
                        "PAYMENT_UPDATED",
                        "FAKE_GATEWAY"
                );

        ReflectionTestUtils.setField(
                registro,
                "id",
                id
        );

        ReflectionTestUtils.setField(
                registro,
                "dataRecebimento",
                Instant.parse(
                        "2026-07-28T20:30:00Z"
                )
        );

        if (duplicado) {
            registro.sinalizarDuplicidade();
        }

        registro.marcarComoProcessado();

        return registro;
    }
}