package br.com.api.pedidos.payment.webhook.document.service;

import br.com.api.pedidos.payment.webhook.document.dto
        .RegistroOperacionalWebhookPagamentoFiltroDTO;
import br.com.api.pedidos.payment.webhook.document.dto
        .RegistroOperacionalWebhookPagamentoResponseDTO;
import br.com.api.pedidos.payment.webhook.document.entity
        .RegistroOperacionalWebhookPagamento;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.pagination.util.PaginacaoUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class RegistroOperacionalWebhookPagamentoConsultaService {

    /*
     * Chave:
     * nome aceito no parametro sort da URL.
     *
     * Valor:
     * nome do atributo dentro do documento MongoDB.
     */
    private static final Map<String, String> CAMPOS_ORDENACAO =
            Map.ofEntries(
                    Map.entry("id", "id"),
                    Map.entry("eventId", "eventId"),
                    Map.entry(
                            "codigoTransacao",
                            "codigoTransacao"
                    ),
                    Map.entry(
                            "statusRecebido",
                            "statusRecebido"
                    ),
                    Map.entry(
                            "statusProcessamento",
                            "statusProcessamento"
                    ),
                    Map.entry("requestId", "requestId"),
                    Map.entry("tipoEvento", "tipoEvento"),
                    Map.entry("origem", "origem"),
                    Map.entry(
                            "dataRecebimento",
                            "dataRecebimento"
                    ),
                    Map.entry(
                            "dataProcessamento",
                            "dataProcessamento"
                    ),
                    Map.entry(
                            "duracaoProcessamentoMs",
                            "duracaoProcessamentoMs"
                    ),
                    Map.entry("duplicado", "duplicado")
            );

    private static final Sort ORDENACAO_PADRAO =
            Sort.by(
                    Sort.Order.desc("dataRecebimento")
            );

    private final MongoTemplate mongoTemplate;

    public RegistroOperacionalWebhookPagamentoConsultaService(
            MongoTemplate mongoTemplate
    ) {
        this.mongoTemplate = mongoTemplate;
    }

    public PaginaResponseDTO<
            RegistroOperacionalWebhookPagamentoResponseDTO
            > consultar(
            RegistroOperacionalWebhookPagamentoFiltroDTO filtro,
            Pageable pageable
    ) {
        validarFiltro(filtro);

        Pageable pageableValidado =
                PaginacaoUtils.normalizar(
                        pageable,
                        CAMPOS_ORDENACAO,
                        ORDENACAO_PADRAO
                );

        Query query = criarQuery(filtro);

        long totalElementos = mongoTemplate.count(
                query,
                RegistroOperacionalWebhookPagamento.class
        );

        query.with(pageableValidado);

        List<RegistroOperacionalWebhookPagamentoResponseDTO>
                conteudo =
                mongoTemplate.find(
                                query,
                                RegistroOperacionalWebhookPagamento.class
                        )
                        .stream()
                        .map(
                                RegistroOperacionalWebhookPagamentoResponseDTO
                                        ::from
                        )
                        .toList();

        Page<RegistroOperacionalWebhookPagamentoResponseDTO>
                pagina =
                new PageImpl<>(
                        conteudo,
                        pageableValidado,
                        totalElementos
                );

        return PaginaResponseDTO.from(pagina);
    }

    private Query criarQuery(
            RegistroOperacionalWebhookPagamentoFiltroDTO filtro
    ) {
        Query query = new Query();

        adicionarCriterioTextoExato(
                query,
                "eventId",
                filtro.eventId()
        );

        adicionarCriterioTextoExato(
                query,
                "codigoTransacao",
                filtro.codigoTransacao()
        );

        if (filtro.statusProcessamento() != null) {
            query.addCriteria(
                    Criteria.where("statusProcessamento")
                            .is(filtro.statusProcessamento())
            );
        }

        adicionarCriterioPeriodo(
                query,
                filtro.dataInicio(),
                filtro.dataFim()
        );

        if (filtro.duplicado() != null) {
            query.addCriteria(
                    Criteria.where("duplicado")
                            .is(filtro.duplicado())
            );
        }

        return query;
    }

    private void adicionarCriterioTextoExato(
            Query query,
            String nomeCampo,
            String valor
    ) {
        String valorNormalizado =
                normalizarTextoOpcional(valor);

        if (valorNormalizado == null) {
            return;
        }

        query.addCriteria(
                Criteria.where(nomeCampo)
                        .is(valorNormalizado)
        );
    }

    private void adicionarCriterioPeriodo(
            Query query,
            Instant dataInicio,
            Instant dataFim
    ) {
        if (dataInicio == null && dataFim == null) {
            return;
        }

        Criteria criterioData =
                Criteria.where("dataRecebimento");

        if (dataInicio != null) {
            criterioData = criterioData.gte(dataInicio);
        }

        if (dataFim != null) {
            criterioData = criterioData.lte(dataFim);
        }

        query.addCriteria(criterioData);
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private void validarFiltro(
            RegistroOperacionalWebhookPagamentoFiltroDTO filtro
    ) {
        if (filtro == null) {
            throw new IllegalArgumentException(
                    "Filtro da consulta é obrigatório"
            );
        }

        validarPeriodo(
                filtro.dataInicio(),
                filtro.dataFim()
        );
    }

    private void validarPeriodo(
            Instant dataInicio,
            Instant dataFim
    ) {
        if (dataInicio == null || dataFim == null) {
            return;
        }

        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException(
                    "Data inicial não pode ser posterior à data final"
            );
        }
    }
}