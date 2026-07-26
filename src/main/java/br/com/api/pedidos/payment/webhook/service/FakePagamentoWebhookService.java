package br.com.api.pedidos.payment.webhook.service;

import br.com.api.pedidos.payment.adapter.fake.GatewayPagamentoFakeConsulta;
import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.facade.CheckoutFacade;
import br.com.api.pedidos.payment.webhook.dto.FakePagamentoWebhookDTO;
import br.com.api.pedidos.payment.webhook.entity.WebhookPagamentoRecebido;
import br.com.api.pedidos.payment.webhook.service.result.ResultadoRegistroWebhook;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FakePagamentoWebhookService {

    private static final Logger log =
            LoggerFactory.getLogger(FakePagamentoWebhookService.class);

    private static final String TIPO_PAYMENT_UPDATED = "PAYMENT_UPDATED";

    private final ObjectMapper objectMapper;
    private final AssinaturaWebhookFakeService assinaturaWebhookFakeService;
    private final GatewayPagamentoFakeConsulta gatewayPagamentoFakeConsulta;
    private final CheckoutFacade checkoutFacade;
    private final WebhookPagamentoRecebidoService webhookPagamentoRecebidoService;

    public FakePagamentoWebhookService(
            ObjectMapper objectMapper,
            AssinaturaWebhookFakeService assinaturaWebhookFakeService,
            GatewayPagamentoFakeConsulta gatewayPagamentoFakeConsulta,
            CheckoutFacade checkoutFacade,
            WebhookPagamentoRecebidoService webhookPagamentoRecebidoService) {
        this.objectMapper = objectMapper;
        this.assinaturaWebhookFakeService = assinaturaWebhookFakeService;
        this.gatewayPagamentoFakeConsulta = gatewayPagamentoFakeConsulta;
        this.checkoutFacade = checkoutFacade;
        this.webhookPagamentoRecebidoService =
                webhookPagamentoRecebidoService;
    }

    public PagamentoResponseDTO processarWebhook(
            String corpoOriginal,
            String assinatura
    ) {
        assinaturaWebhookFakeService.validarAssinatura(
                corpoOriginal,
                assinatura
        );

        FakePagamentoWebhookDTO dto = converter(corpoOriginal);
        validarWebhook(dto);

        log.info(
                "Webhook recebido. eventId={} status={}",
                dto.eventId(),
                dto.statusPagamento()
        );

        ResultadoRegistroWebhook resultadoRegistro =
                webhookPagamentoRecebidoService
                        .registrarOuBuscarExistente(
                                dto,
                                corpoOriginal
                        );

        if (!resultadoRegistro.deveProcessar()) {
            log.info(
                    "Webhook duplicado ignorado. eventId={}",
                    resultadoRegistro
                            .evento()
                            .getEventId()
            );

            return checkoutFacade
                    .buscarPagamentoPorCodigoTransacao(
                            resultadoRegistro
                                    .evento()
                                    .getCodigoTransacao()
                    );
        }

        return processarEventoRecebido(resultadoRegistro.evento());
    }

    private PagamentoResponseDTO processarEventoRecebido(
            WebhookPagamentoRecebido evento
    ) {
        try {
            // Criado pra simular que o gateway/banco alterou o status da transação no ambiente externo.
            gatewayPagamentoFakeConsulta.simularAtualizacaoExterna(
                    evento.getCodigoTransacao(),
                    evento.getStatusRecebido()
            );

            PagamentoResponseDTO pagamentoResponseDTO =
                    checkoutFacade.processarWebhookPagamento(
                            evento.getCodigoTransacao()
                    );

            webhookPagamentoRecebidoService.marcarComoProcessado(
                    evento
            );

            log.info(
                    "Webhook processado. eventId={} status={}",
                    evento.getEventId(),
                    evento.getStatusRecebido()
            );

            return pagamentoResponseDTO;

        } catch (Exception exception) {
            log.error(
                    "Erro ao processar webhook. eventId={}",
                    evento.getEventId(),
                    exception
            );

            webhookPagamentoRecebidoService.marcarComoErro(
                    evento,
                    exception
            );

            throw exception;
        }
    }

    private FakePagamentoWebhookDTO converter(String corpoOriginal) {
        try {
            return objectMapper.readValue(
                    corpoOriginal,
                    FakePagamentoWebhookDTO.class
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Payload do webhook inválido",
                    e
            );
        }
    }

    private void validarWebhook(FakePagamentoWebhookDTO dto) {
        if (dto.eventId() == null || dto.eventId().isBlank()) {
            throw new IllegalArgumentException(
                    "EventId do webhook é obrigatório"
            );
        }

        if (!TIPO_PAYMENT_UPDATED.equals(dto.tipo())) {
            throw new IllegalArgumentException(
                    "Tipo de evento não suportado: " + dto.tipo()
            );
        }

        if (dto.codigoTransacao() == null
                || dto.codigoTransacao().isBlank()) {
            throw new IllegalArgumentException(
                    "Código da transação é obrigatório"
            );
        }

        if (dto.statusPagamento() == null) {
            throw new IllegalArgumentException(
                    "Status do pagamento é obrigatório"
            );
        }

        if (dto.statusPagamento() != StatusPagamento.APROVADO
                && dto.statusPagamento() != StatusPagamento.RECUSADO
                && dto.statusPagamento() != StatusPagamento.PENDENTE) {
            throw new IllegalArgumentException(
                    "Status não permitido via webhook: "
                            + dto.statusPagamento()
            );
        }
    }
}
