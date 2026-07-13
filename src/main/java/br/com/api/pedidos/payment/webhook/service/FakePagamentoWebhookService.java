package br.com.api.pedidos.payment.webhook.service;

import br.com.api.pedidos.payment.adapter.fake.GatewayPagamentoFakeConsulta;
import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.facade.CheckoutFacade;
import br.com.api.pedidos.payment.webhook.dto.FakePagamentoWebhookDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FakePagamentoWebhookService {

    private static final String TIPO_PAYMENT_UPDATED = "PAYMENT_UPDATED";

    private final ObjectMapper objectMapper;
    private final AssinaturaWebhookFakeService assinaturaWebhookFakeService;
    private final GatewayPagamentoFakeConsulta gatewayPagamentoFakeConsulta;
    private final CheckoutFacade checkoutFacade;

    public FakePagamentoWebhookService(
            ObjectMapper objectMapper,
            AssinaturaWebhookFakeService assinaturaWebhookFakeService,
            GatewayPagamentoFakeConsulta gatewayPagamentoFakeConsulta,
            CheckoutFacade checkoutFacade) {
        this.objectMapper = objectMapper;
        this.assinaturaWebhookFakeService = assinaturaWebhookFakeService;
        this.gatewayPagamentoFakeConsulta = gatewayPagamentoFakeConsulta;
        this.checkoutFacade = checkoutFacade;
    }

    @Transactional
    public PagamentoResponseDTO processarWebhook(
            String corpoOriginal,
            String assinatura) {
        assinaturaWebhookFakeService.validarAssinatura(
                corpoOriginal, assinatura
        );

        var dto = converter(corpoOriginal);
        validarWebhook(dto);

        // Criado pra simular que o gateway/banco alterou o status da transação no ambiente externo.
        gatewayPagamentoFakeConsulta.simularAtualizacaoExterna(
                dto.codigoTransacao(),
                dto.statusPagamento()
        );

        return checkoutFacade.processarWebhookPagamento(
                dto.codigoTransacao()
        );
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
            throw new IllegalArgumentException("EventId do webhook é obrigatório");
        }

        if (!TIPO_PAYMENT_UPDATED.equals(dto.tipo())) {
            throw new IllegalArgumentException(
                    "Tipo de evento não suportado: " + dto.tipo()
            );
        }

        if (dto.codigoTransacao() == null || dto.codigoTransacao().isBlank()) {
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
