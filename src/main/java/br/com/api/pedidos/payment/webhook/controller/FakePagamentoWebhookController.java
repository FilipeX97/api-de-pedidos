package br.com.api.pedidos.payment.webhook.controller;

import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.dto.openapi.RespostaPagamentoOpenApiDTO;
import br.com.api.pedidos.payment.webhook.dto.FakePagamentoWebhookDTO;
import br.com.api.pedidos.payment.webhook.service.FakePagamentoWebhookService;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroOpenApiDTO;
import br.com.api.pedidos.shared.response.RespostaApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Webhook simulado de pagamento",
        description = """
                Recebimento de eventos simulados enviados pelo gateway
                fake de pagamentos.

                Este endpoint é público e não utiliza autenticação JWT.

                A autenticidade da requisição é validada pelo header
                X-Fake-Gateway-Signature, que deve conter uma assinatura
                HMAC-SHA256 calculada sobre o corpo original da requisição.

                O campo eventId torna o processamento do evento
                idempotente.

                As transações assíncronas de PIX e boleto são persistidas,
                portanto seus estados não são perdidos quando a aplicação
                é reiniciada.
                """
)
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

    @Operation(
            summary = "Receber atualização de pagamento",
            description = """
                    Recebe um evento simulado de atualização de pagamento.

                    A requisição não utiliza Bearer Token.

                    O header X-Fake-Gateway-Signature deve conter a
                    assinatura HMAC-SHA256 do corpo original da requisição,
                    utilizando o segredo configurado para o webhook fake.

                    O único tipo de evento aceito é PAYMENT_UPDATED.

                    Os status permitidos são:

                    PENDENTE,
                    APROVADO e
                    RECUSADO.

                    Quando o status recebido é APROVADO, o pagamento pendente
                    é confirmado. Caso o pedido esteja aguardando pagamento,
                    ele também é marcado como PAGO.

                    Quando o status é RECUSADO, o pagamento pendente é
                    marcado como recusado.

                    Quando o status é PENDENTE, nenhuma confirmação ou
                    recusa é aplicada.

                    O campo eventId impede o processamento repetido do mesmo
                    evento.

                    Um evento já processado retorna novamente os dados
                    atuais do pagamento sem executar o processamento outra
                    vez.

                    Um evento que tenha terminado anteriormente com erro
                    pode ser reprocessado.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Webhook processado ou evento duplicado recuperado
                            com sucesso
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPagamentoOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pagamentoAprovado",
                                            summary = """
                                                    Pagamento aprovado pelo
                                                    webhook
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPagamento": 15,
                                                        "idPedido": 10,
                                                        "valor": 300.00,
                                                        "formaPagamento": "PIX",
                                                        "statusPagamento": "APROVADO",
                                                        "codigoTransacao": "PIX-550e8400-e29b-41d4-a716-446655440000",
                                                        "mensagem": "Pagamento confirmado pelo gateway fake via webhook",
                                                        "dataCriacao": "2026-07-19T18:00:00"
                                                      },
                                                      "mensagem": "Webhook de pagamento processado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "pagamentoRecusado",
                                            summary = """
                                                    Pagamento recusado pelo
                                                    webhook
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPagamento": 16,
                                                        "idPedido": 11,
                                                        "valor": 750.00,
                                                        "formaPagamento": "BOLETO",
                                                        "statusPagamento": "RECUSADO",
                                                        "codigoTransacao": "BOL-650e8400-e29b-41d4-a716-446655440000",
                                                        "mensagem": "Pagamento recusado pelo gateway fake via webhook",
                                                        "dataCriacao": "2026-07-19T18:10:00"
                                                      },
                                                      "mensagem": "Webhook de pagamento processado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "pagamentoPendente",
                                            summary = """
                                                    Transação permanece
                                                    pendente
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPagamento": 17,
                                                        "idPedido": 12,
                                                        "valor": 120.00,
                                                        "formaPagamento": "PIX",
                                                        "statusPagamento": "PENDENTE",
                                                        "codigoTransacao": "PIX-750e8400-e29b-41d4-a716-446655440000",
                                                        "mensagem": "PIX gerado e aguardando pagamento",
                                                        "dataCriacao": "2026-07-19T18:20:00"
                                                      },
                                                      "mensagem": "Webhook de pagamento processado com sucesso"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Header ausente, payload inválido, evento não
                            suportado ou transação ausente no gateway fake
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "headerAssinaturaAusente",
                                            summary = """
                                                    Header obrigatório não
                                                    enviado
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Header obrigatório não enviado: X-Fake-Gateway-Signature"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "corpoNaoEnviado",
                                            summary = """
                                                    Corpo obrigatório não
                                                    enviado
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "JSON inválido ou valor incompatível com o tipo esperado"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "payloadInvalido",
                                            summary = """
                                                    Corpo não pode ser
                                                    convertido
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Payload do webhook inválido"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "eventIdAusente",
                                            summary = """
                                                    Identificador do evento
                                                    não informado
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "EventId do webhook é obrigatório"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "tipoEventoNaoSuportado",
                                            summary = """
                                                    Tipo de evento não aceito
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Tipo de evento não suportado: ORDER_UPDATED"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "codigoTransacaoAusente",
                                            summary = """
                                                    Código da transação não
                                                    informado
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Código da transação é obrigatório"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "statusAusente",
                                            summary = """
                                                    Status do pagamento não
                                                    informado
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Status do pagamento é obrigatório"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "statusNaoPermitido",
                                            summary = """
                                                    Status existente, mas não
                                                    permitido no webhook
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Status não permitido via webhook: CANCELADO"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "transacaoNaoEncontradaNoGateway",
                                            summary = """
                                                    Código nunca registrado no gateway fake
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Transação não encontrada no gateway fake"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            Assinatura do webhook ausente ou inválida
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "assinaturaVazia",
                                            summary = """
                                                    Header enviado sem uma
                                                    assinatura
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Assinatura do webhook não enviada"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "assinaturaInvalida",
                                            summary = """
                                                    Assinatura não corresponde
                                                    ao corpo recebido
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Assinatura do webhook inválida"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            Pagamento não encontrado pelo código da transação
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pagamentoNaoEncontrado",
                                    summary = """
                                            Código não corresponde a um
                                            pagamento persistido
                                            """,
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Pagamento não encontrado pela transação"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                            Estado atual do pagamento ou do pedido não permite
                            processar a atualização
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pagamentoNaoPodeSerAprovado",
                                            summary = """
                                                    Pagamento não está
                                                    pendente
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Somente pagamento pendente pode ser aprovado pelo gateway"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "pagamentoNaoPodeSerRecusado",
                                            summary = """
                                                    Pagamento não está
                                                    pendente
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Somente pagamento pendente pode ser recusado pelo gateway"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "pedidoNaoAguardaPagamento",
                                            summary = """
                                                    Pedido em status
                                                    incompatível
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Somente pedido aguardando pagamento pode ser confirmado pelo webhook. Status atual: CANCELADO"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping
    public RespostaApi<PagamentoResponseDTO> receberWebhookPagamento(
            @Parameter(
                    name = HEADER_ASSINATURA,
                    description = """
                            Assinatura HMAC-SHA256 do corpo original da
                            requisição.

                            A assinatura deve ser gerada utilizando o segredo
                            configurado em FAKE_WEBHOOK_SECRET.

                            O resultado deve ser enviado como uma sequência
                            hexadecimal minúscula com 64 caracteres.

                            Qualquer alteração no corpo, incluindo espaços
                            e quebras de linha, modifica a assinatura.
                            """,
                    in = ParameterIn.HEADER,
                    required = true,
                    example =
                            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                    schema = @Schema(
                            type = "string",
                            minLength = 64,
                            maxLength = 64,
                            pattern = "^[0-9a-f]{64}$"
                    )
            )
            @RequestHeader(HEADER_ASSINATURA) String assinatura,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                            Evento de atualização enviado pelo gateway fake.

                            O JSON exibido no Swagger representa o DTO
                            utilizado internamente, embora o controller
                            preserve o corpo original como String para
                            validar a assinatura.
                            """,
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            FakePagamentoWebhookDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pagamentoAprovado",
                                            summary = """
                                                    Confirmação de pagamento
                                                    """,
                                            value = """
                                                    {
                                                      "eventId": "evt-pagamento-550e8400-e29b-41d4-a716-446655440000",
                                                      "tipo": "PAYMENT_UPDATED",
                                                      "codigoTransacao": "PIX-550e8400-e29b-41d4-a716-446655440000",
                                                      "statusPagamento": "APROVADO",
                                                      "dataEvento": "2026-07-19T18:30:00Z"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "pagamentoRecusado",
                                            summary = """
                                                    Recusa de pagamento
                                                    """,
                                            value = """
                                                    {
                                                      "eventId": "evt-pagamento-650e8400-e29b-41d4-a716-446655440000",
                                                      "tipo": "PAYMENT_UPDATED",
                                                      "codigoTransacao": "BOL-650e8400-e29b-41d4-a716-446655440000",
                                                      "statusPagamento": "RECUSADO",
                                                      "dataEvento": "2026-07-19T18:35:00Z"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
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
