package br.com.api.pedidos.payment.webhook.service;

import br.com.api.pedidos.payment.webhook.dto.FakePagamentoWebhookDTO;
import br.com.api.pedidos.payment.webhook.entity.WebhookPagamentoRecebido;
import br.com.api.pedidos.payment.webhook.repository.WebhookPagamentoRecebidoRepository;
import br.com.api.pedidos.payment.webhook.service.result.ResultadoRegistroWebhook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WebhookPagamentoRecebidoService {

    private final WebhookPagamentoRecebidoRepository webhookPagamentoRecebidoRepository;

    public WebhookPagamentoRecebidoService(
            WebhookPagamentoRecebidoRepository webhookPagamentoRecebidoRepository) {
        this.webhookPagamentoRecebidoRepository =
                webhookPagamentoRecebidoRepository;
    }

    @Transactional(readOnly = true)
    public Optional<WebhookPagamentoRecebido> buscarPorEventId(
            String eventId
    ) {
        return webhookPagamentoRecebidoRepository.findByEventId(eventId);
    }

    public ResultadoRegistroWebhook registrarOuBuscarExistente(
            FakePagamentoWebhookDTO fakePagamentoWebhookDTO,
            String payloadOriginal
    ) {
        Optional<WebhookPagamentoRecebido> existente =
                webhookPagamentoRecebidoRepository.findByEventId(
                        fakePagamentoWebhookDTO.eventId()
                );

        if (existente.isPresent()) {
            return new ResultadoRegistroWebhook(
                    existente.get(),
                    false
            );
        }

        try {
            WebhookPagamentoRecebido webhook =
                    new WebhookPagamentoRecebido(
                            fakePagamentoWebhookDTO.eventId(),
                            fakePagamentoWebhookDTO.codigoTransacao(),
                            fakePagamentoWebhookDTO.statusPagamento(),
                            payloadOriginal
                    );

            WebhookPagamentoRecebido salvo =
                    webhookPagamentoRecebidoRepository.saveAndFlush(webhook);

            return new ResultadoRegistroWebhook(
                    salvo,
                    true
            );

        } catch (DataIntegrityViolationException e) {
            WebhookPagamentoRecebido webhookExistente =
                    webhookPagamentoRecebidoRepository
                            .findByEventId(
                                    fakePagamentoWebhookDTO.eventId()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Evento de webhook duplicado, mas não foi possível recuperar o registro existente"
                                    )
                            );

            return new ResultadoRegistroWebhook(
                    webhookExistente,
                    false
            );
        }
    }

    @Transactional
    public WebhookPagamentoRecebido marcarComoProcessado(
            WebhookPagamentoRecebido webhook
    ) {
        validarWebhook(webhook);

        webhook.marcarComoProcessado();

        return webhookPagamentoRecebidoRepository.saveAndFlush(webhook);
    }

    @Transactional
    public WebhookPagamentoRecebido marcarComoErro(
            WebhookPagamentoRecebido webhook,
            Exception exception
    ) {
        validarWebhook(webhook);

        webhook.marcarComoErro(
                extrairMensagemErro(exception)
        );

        return webhookPagamentoRecebidoRepository.saveAndFlush(webhook);
    }

    private void validarWebhook(WebhookPagamentoRecebido webhook) {
        if (webhook == null) {
            throw new IllegalArgumentException(
                    "Webhook recebido é obrigatório"
            );
        }
    }

    private String extrairMensagemErro(Exception exception) {
        if (exception == null) {
            return "Erro não informado";
        }

        if (exception.getMessage() != null
                && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }

        return exception.getClass().getSimpleName();
    }
}
