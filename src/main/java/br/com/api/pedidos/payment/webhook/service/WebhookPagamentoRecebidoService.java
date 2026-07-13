package br.com.api.pedidos.payment.webhook.service;

import br.com.api.pedidos.payment.webhook.dto.FakePagamentoWebhookDTO;
import br.com.api.pedidos.payment.webhook.entity.WebhookPagamentoRecebido;
import br.com.api.pedidos.payment.webhook.repository.WebhookPagamentoRecebidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WebhookPagamentoRecebidoService {

    private final WebhookPagamentoRecebidoRepository webhookPagamentoRecebidoRepository;

    public WebhookPagamentoRecebidoService(
            WebhookPagamentoRecebidoRepository webhookPagamentoRecebidoRepository) {
        this.webhookPagamentoRecebidoRepository = webhookPagamentoRecebidoRepository;
    }

    @Transactional(readOnly = true)
    public Optional<WebhookPagamentoRecebido> buscarPorEventId(String eventId) {
        return webhookPagamentoRecebidoRepository.findByEventId(eventId);
    }

    @Transactional
    public WebhookPagamentoRecebido registrar(
            FakePagamentoWebhookDTO fakePagamentoWebhookDTO,
            String payloadOriginal) {
        WebhookPagamentoRecebido webhook = new WebhookPagamentoRecebido(
                fakePagamentoWebhookDTO.eventId(),
                fakePagamentoWebhookDTO.codigoTransacao(),
                fakePagamentoWebhookDTO.statusPagamento(),
                payloadOriginal
        );

        return webhookPagamentoRecebidoRepository.saveAndFlush(webhook);
    }


}
