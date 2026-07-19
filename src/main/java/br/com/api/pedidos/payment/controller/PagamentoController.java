package br.com.api.pedidos.payment.controller;

import br.com.api.pedidos.payment.dto.PagamentoRequestDTO;
import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.dto.openapi.RespostaListaPagamentosOpenApiDTO;
import br.com.api.pedidos.payment.dto.openapi.RespostaPagamentoOpenApiDTO;
import br.com.api.pedidos.payment.facade.CheckoutFacade;
import br.com.api.pedidos.security.service.UsuarioLogadoService;
import br.com.api.pedidos.shared.idempotency.service.IdempotencyService;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroOpenApiDTO;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroValidacaoOpenApiDTO;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Pagamentos",
        description = """
                Processamento e consulta dos pagamentos de um pedido.

                O usuário autenticado pode processar pagamentos e consultar
                somente os pagamentos dos próprios pedidos.
                """
)
@ApiResponses({
        @ApiResponse(
                responseCode = "401",
                description = """
                        Token não enviado ou contexto de autenticação inválido
                        """,
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(
                                implementation =
                                        RespostaErroOpenApiDTO.class
                        ),
                        examples = {
                                @ExampleObject(
                                        name = "tokenNaoEnviado",
                                        summary = "Token não enviado",
                                        value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Token não enviado"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "tokenInvalidoOuExpirado",
                                        summary = "Token inválido ou expirado",
                                        value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Token inválido ou expirado"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "tokenBloqueado",
                                        summary = "Token bloqueado",
                                        value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Token bloqueado. Faça login novamente."
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "ipOuUserAgentInvalido",
                                        summary = """
                                                IP ou User-Agent diferente do token
                                                """,
                                        value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "IP ou UserAgent inválido"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "tokenUsuarioInvalido",
                                        summary = """
                                                Token não pertence ao usuário carregado
                                                """,
                                        value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Token inválido"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "usuarioDesativado",
                                        summary = "Usuário do token está desativado",
                                        value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Usuário desativado"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "senhaAlterada",
                                        summary = """
                                                Senha alterada após a emissão do token
                                                """,
                                        value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Senha alterada"
                                                }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode = "429",
                description = "Limite temporário de requisições excedido",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(
                                implementation =
                                        RespostaErroOpenApiDTO.class
                        ),
                        examples = @ExampleObject(
                                name = "muitasRequisicoes",
                                summary = "Limite de requisições excedido",
                                value = """
                                        {
                                          "sucesso": false,
                                          "dados": null,
                                          "mensagem": "Muitas requisições. Aguarde um momento."
                                        }
                                        """
                        )
                )
        )
})
@RestController
@RequestMapping("/orders/{idPedido}/payments")
public class PagamentoController {

    private final CheckoutFacade checkoutFacade;
    private final UsuarioLogadoService usuarioLogadoService;
    private final IdempotencyService idempotencyService;

    public PagamentoController(
            CheckoutFacade checkoutFacade,
            UsuarioLogadoService usuarioLogadoService,
            IdempotencyService idempotencyService) {
        this.checkoutFacade = checkoutFacade;
        this.usuarioLogadoService = usuarioLogadoService;
        this.idempotencyService = idempotencyService;
    }

    @Operation(
            summary = "Processar pagamento",
            description = """
                    Processa o pagamento de um pedido pertencente ao usuário
                    autenticado.

                    Somente pedidos com status CRIADO e que possuam itens
                    podem iniciar um pagamento.

                    A operação exige o header Idempotency-Key.

                    Um pagamento com cartão pode ser aprovado ou recusado.
                    Pagamentos PIX e boleto são inicialmente pendentes.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = """
                            Pagamento processado ou resposta idempotente
                            recuperada
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPagamentoOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pixPendente",
                                            summary = """
                                                    Pagamento PIX criado como pendente
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPagamento": 15,
                                                        "idPedido": 10,
                                                        "valor": 300.00,
                                                        "formaPagamento": "PIX",
                                                        "statusPagamento": "PENDENTE",
                                                        "codigoTransacao": "PIX-550e8400-e29b-41d4-a716-446655440000",
                                                        "mensagem": "PIX gerado. QR Code: QR-CODE-FAKE-PIX-550e8400-e29b-41d4-a716-446655440000 | Copia e cola: 00020126360014BR.GOV.BCB.PIX",
                                                        "dataCriacao": "2026-07-18T14:45:00"
                                                      },
                                                      "mensagem": "Pagamento processado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "cartaoAprovado",
                                            summary = """
                                                    Pagamento com cartão aprovado
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPagamento": 16,
                                                        "idPedido": 10,
                                                        "valor": 300.00,
                                                        "formaPagamento": "CARTAO_CREDITO",
                                                        "statusPagamento": "APROVADO",
                                                        "codigoTransacao": "CARD-550e8400-e29b-41d4-a716-446655440000",
                                                        "mensagem": "Pagamento autorizado pela operadora",
                                                        "dataCriacao": "2026-07-18T14:47:00"
                                                      },
                                                      "mensagem": "Pagamento processado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "cartaoRecusado",
                                            summary = """
                                                    Pagamento com cartão recusado
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPagamento": 17,
                                                        "idPedido": 10,
                                                        "valor": 5200.00,
                                                        "formaPagamento": "CARTAO_CREDITO",
                                                        "statusPagamento": "RECUSADO",
                                                        "codigoTransacao": "CARD-650e8400-e29b-41d4-a716-446655440000",
                                                        "mensagem": "Pagamento recusado pela operadora",
                                                        "dataCriacao": "2026-07-18T14:50:00"
                                                      },
                                                      "mensagem": "Pagamento processado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "respostaIdempotente",
                                            summary = """
                                                    Pagamento já processado anteriormente
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPagamento": 15,
                                                        "idPedido": 10,
                                                        "valor": 300.00,
                                                        "formaPagamento": "PIX",
                                                        "statusPagamento": "PENDENTE",
                                                        "codigoTransacao": "PIX-550e8400-e29b-41d4-a716-446655440000",
                                                        "mensagem": "PIX gerado. QR Code: QR-CODE-FAKE-PIX-550e8400-e29b-41d4-a716-446655440000 | Copia e cola: 00020126360014BR.GOV.BCB.PIX",
                                                        "dataCriacao": "2026-07-18T14:45:00"
                                                      },
                                                      "mensagem": "Requisição já processada anteriormente (idempotência)"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Dados, JSON ou Idempotency-Key inválidos
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    oneOf = {
                                            RespostaErroValidacaoOpenApiDTO.class,
                                            RespostaErroOpenApiDTO.class
                                    }
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "formaPagamentoAusente",
                                            summary = """
                                                    Forma de pagamento não informada
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": {
                                                        "formaPagamento": "Forma de pagamento é obrigatória"
                                                      },
                                                      "mensagem": "Dados inválidos"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "formaPagamentoInvalida",
                                            summary = """
                                                    Valor incompatível com o enum
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
                                            name = "headerIdempotenciaAusente",
                                            summary = """
                                                    Header obrigatório não enviado
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Header obrigatório não enviado: Idempotency-Key"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "chaveIdempotenciaVazia",
                                            summary = "Chave idempotente vazia",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Idempotency-Key é obrigatória"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "chaveIdempotenciaMuitoLonga",
                                            summary = """
                                                    Chave maior que 255 caracteres
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Idempotency-Key deve ter no máximo 255 caracteres"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            Pedido inexistente ou pertencente a outro usuário
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoEncontrado",
                                    summary = "Pedido não encontrado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Pedido não encontrado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                            Estado do pedido, cupom ou chave idempotente
                            incompatível com a operação
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "statusPedidoInvalido",
                                            summary = """
                                                    Pedido não está com status CRIADO
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Somente pedido com status CRIADO pode iniciar pagamento. Status atual: PAGO"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "pedidoSemItens",
                                            summary = "Pedido vazio",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Não é possível pagar um pedido sem itens"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "cupomIndisponivel",
                                            summary = """
                                                    Cupom não pode mais registrar utilização
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Cupom indisponível"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "chaveReutilizada",
                                            summary = """
                                                    Idempotency-Key reutilizada com outro corpo
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Idempotency-Key reutilizada com payload diferente"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaApi<PagamentoResponseDTO> processarPagamento(
            @Parameter(
                    name = "Idempotency-Key",
                    description = """
                            Chave única da operação.

                            A chave pode possuir no máximo 255 caracteres
                            e permanece válida por 24 horas.
                            """,
                    in = ParameterIn.HEADER,
                    required = true,
                    example =
                            "pagamento-550e8400-e29b-41d4-a716-446655440000",
                    schema = @Schema(
                            type = "string",
                            maxLength = 255
                    )
            )
            @RequestHeader("Idempotency-Key")
            String key,

            HttpServletRequest request,

            @Parameter(
                    description = "Identificador do pedido",
                    required = true,
                    example = "10"
            )
            @PathVariable
            Long idPedido,
            @Valid @RequestBody PagamentoRequestDTO pagamentoRequestDTO) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return idempotencyService.executar(
                key,
                request,
                pagamentoRequestDTO,
                PagamentoResponseDTO.class,
                () -> checkoutFacade.processarPagamento(
                        idPedido,
                        usuario,
                        pagamentoRequestDTO
                ),
                "Pagamento processado com sucesso"
        );
    }

    @Operation(
            summary = "Listar pagamentos do pedido",
            description = """
                    Retorna todos os pagamentos registrados para um pedido
                    pertencente ao usuário autenticado.

                    Os pagamentos são retornados do mais recente para
                    o mais antigo.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pagamentos encontrados",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaListaPagamentosOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pagamentosEncontrados",
                                            summary = """
                                                    Pedido possui pagamentos
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": [
                                                        {
                                                          "idPagamento": 16,
                                                          "idPedido": 10,
                                                          "valor": 300.00,
                                                          "formaPagamento": "CARTAO_CREDITO",
                                                          "statusPagamento": "APROVADO",
                                                          "codigoTransacao": "CARD-550e8400-e29b-41d4-a716-446655440000",
                                                          "mensagem": "Pagamento autorizado pela operadora",
                                                          "dataCriacao": "2026-07-18T14:47:00"
                                                        },
                                                        {
                                                          "idPagamento": 15,
                                                          "idPedido": 10,
                                                          "valor": 300.00,
                                                          "formaPagamento": "PIX",
                                                          "statusPagamento": "PENDENTE",
                                                          "codigoTransacao": "PIX-650e8400-e29b-41d4-a716-446655440000",
                                                          "mensagem": "PIX gerado. QR Code: QR-CODE-FAKE-PIX-650e8400-e29b-41d4-a716-446655440000",
                                                          "dataCriacao": "2026-07-18T14:45:00"
                                                        }
                                                      ],
                                                      "mensagem": "Pagamentos encontrados"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "nenhumPagamento",
                                            summary = """
                                                    Pedido ainda não possui pagamentos
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": [],
                                                      "mensagem": "Pagamentos encontrados"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Identificador informado em formato inválido
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "idPedidoInvalido",
                                    summary = "Identificador incompatível",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Parâmetro inválido: idPedido"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            Pedido inexistente ou pertencente a outro usuário
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoEncontrado",
                                    summary = "Pedido não encontrado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Pedido não encontrado"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public RespostaApi<List<PagamentoResponseDTO>> listarPagamentosDoPedido(
            @Parameter(
                    description = "Identificador do pedido",
                    required = true,
                    example = "10"
            )
            @PathVariable Long idPedido) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                checkoutFacade.listarPagamentosDoPedido(idPedido, usuario),
                "Pagamentos encontrados"
        );
    }

}
