package br.com.api.pedidos.payment.webhook.controller;

import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.webhook.service.FakePagamentoWebhookService;
import br.com.api.pedidos.shared.response.RespostaApi;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/payments/fake")
public class FakePagamentoWebhookController {

    private static final String HEADER_ASSINATURA =
            "X-Fake-Gateway-Signature";

    private final FakePagamentoWebhookService fakePagamentoWebhookService;

    public FakePagamentoWebhookController(
            FakePagamentoWebhookService fakePagamentoWebhookService) {
        this.fakePagamentoWebhookService = fakePagamentoWebhookService;
    }

    @PostMapping
    public RespostaApi<PagamentoResponseDTO> receberWebhookPagamento(
            @RequestHeader(HEADER_ASSINATURA) String assinatura,
            @RequestBody String corpoOriginal) {
        return RespostaApi.sucesso(
                fakePagamentoWebhookService.processarWebhook(
                        corpoOriginal,
                        assinatura
                ),
                "Webhook de pagamento processado com sucesso"
        );
    }
}
