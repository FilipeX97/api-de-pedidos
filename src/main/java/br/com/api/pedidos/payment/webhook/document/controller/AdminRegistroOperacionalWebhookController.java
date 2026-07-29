package br.com.api.pedidos.payment.webhook.document.controller;

import br.com.api.pedidos.config.OpenApiConfig;
import br.com.api.pedidos.payment.webhook.document.dto
        .RegistroOperacionalWebhookPagamentoFiltroDTO;
import br.com.api.pedidos.payment.webhook.document.dto
        .RegistroOperacionalWebhookPagamentoResponseDTO;
import br.com.api.pedidos.payment.webhook.document.dto.openapi
        .RespostaPaginaRegistrosOperacionaisWebhookOpenApiDTO;
import br.com.api.pedidos.payment.webhook.document.service
        .RegistroOperacionalWebhookPagamentoConsultaService;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroOpenApiDTO;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.response.RespostaApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Administração de webhooks operacionais",
        description = """
                Consulta administrativa dos registros operacionais e
                documentais de webhooks de pagamento armazenados no MongoDB
                """
)
@RestController
@RequestMapping("/admin/webhooks/payments/operational")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminRegistroOperacionalWebhookController {

    private final RegistroOperacionalWebhookPagamentoConsultaService
            consultaService;

    public AdminRegistroOperacionalWebhookController(
            RegistroOperacionalWebhookPagamentoConsultaService
                    consultaService
    ) {
        this.consultaService = consultaService;
    }

    @Operation(
            summary = "Consultar registros operacionais de webhooks",
            description = """
                    Consulta as tentativas operacionais de webhooks de
                    pagamento armazenadas no MongoDB.

                    O endpoint é destinado à investigação técnica e não
                    substitui o estado transacional mantido no PostgreSQL.

                    Todos os filtros são opcionais.

                    eventId e codigoTransacao utilizam correspondência exata.

                    dataInicio e dataFim são aplicadas sobre o campo
                    dataRecebimento e possuem limites inclusivos.

                    O campo duplicado indica se o eventId já existia no
                    controle transacional do PostgreSQL quando a tentativa
                    foi recebida.

                    A ordenação padrão é dataRecebimento em ordem
                    decrescente.
                    """,
            parameters = {
                    @Parameter(
                            name = "eventId",
                            description = """
                                    Identificador exato do evento enviado
                                    pelo gateway
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "evt-123",
                            schema = @Schema(
                                    type = "string",
                                    maxLength = 150
                            )
                    ),
                    @Parameter(
                            name = "codigoTransacao",
                            description = """
                                    Código exato da transação de pagamento
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "PIX-123",
                            schema = @Schema(
                                    type = "string",
                                    maxLength = 150
                            )
                    ),
                    @Parameter(
                            name = "statusProcessamento",
                            description = """
                                    Status operacional da tentativa de webhook
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "ERRO",
                            schema = @Schema(
                                    type = "string",
                                    allowableValues = {
                                            "RECEBIDO",
                                            "PROCESSADO",
                                            "DUPLICADO",
                                            "ERRO"
                                    }
                            )
                    ),
                    @Parameter(
                            name = "dataInicio",
                            description = """
                                    Instante inicial inclusivo aplicado sobre
                                    dataRecebimento.

                                    Formato ISO-8601 com offset ou UTC.
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "2026-07-28T00:00:00Z",
                            schema = @Schema(
                                    type = "string",
                                    format = "date-time"
                            )
                    ),
                    @Parameter(
                            name = "dataFim",
                            description = """
                                    Instante final inclusivo aplicado sobre
                                    dataRecebimento.

                                    Formato ISO-8601 com offset ou UTC.
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "2026-07-28T23:59:59Z",
                            schema = @Schema(
                                    type = "string",
                                    format = "date-time"
                            )
                    ),
                    @Parameter(
                            name = "duplicado",
                            description = """
                                    Indica se o eventId já existia no
                                    PostgreSQL
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "true",
                            schema = @Schema(
                                    type = "boolean"
                            )
                    ),
                    @Parameter(
                            name = "page",
                            description = """
                                    Número da página.

                                    A primeira página possui índice zero.
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "0",
                            schema = @Schema(
                                    type = "integer",
                                    minimum = "0",
                                    defaultValue = "0"
                            )
                    ),
                    @Parameter(
                            name = "size",
                            description = """
                                    Quantidade de registros por página.

                                    O valor máximo permitido é 100.
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "20",
                            schema = @Schema(
                                    type = "integer",
                                    minimum = "1",
                                    maximum = "100",
                                    defaultValue = "20"
                            )
                    ),
                    @Parameter(
                            name = "sort",
                            description = """
                                    Ordenação no formato campo,direção.

                                    Exemplo:
                                    dataRecebimento,desc

                                    Campos permitidos:
                                    id, eventId, codigoTransacao,
                                    statusRecebido, statusProcessamento,
                                    requestId, tipoEvento, origem,
                                    dataRecebimento, dataProcessamento,
                                    duracaoProcessamentoMs e duplicado.

                                    O parâmetro pode ser repetido para aplicar
                                    mais de uma ordenação.
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "dataRecebimento,desc",
                            schema = @Schema(
                                    type = "string"
                            )
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Registros operacionais consultados com sucesso
                            """,
                    content = @Content(
                            mediaType =
                                    MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPaginaRegistrosOperacionaisWebhookOpenApiDTO
                                                    .class
                            ),
                            examples = @ExampleObject(
                                    name = "paginaRegistrosOperacionais",
                                    summary = """
                                            Página com registros operacionais
                                            encontrados
                                            """,
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "conteudo": [
                                                  {
                                                    "id": "6888c15bd39d9a67112f4567",
                                                    "eventId": "evt-123",
                                                    "codigoTransacao": "PIX-123",
                                                    "statusRecebido": "APROVADO",
                                                    "statusProcessamento": "PROCESSADO",
                                                    "payloadOriginal": "{\\"eventId\\":\\"evt-123\\"}",
                                                    "requestId": "request-abc-123",
                                                    "tipoEvento": "PAYMENT_UPDATED",
                                                    "origem": "FAKE_GATEWAY",
                                                    "dataRecebimento": "2026-07-28T20:30:00Z",
                                                    "dataProcessamento": "2026-07-28T20:30:01Z",
                                                    "duracaoProcessamentoMs": 180,
                                                    "duplicado": false,
                                                    "mensagemErro": null
                                                  }
                                                ],
                                                "paginaAtual": 0,
                                                "totalPaginas": 1,
                                                "totalElementos": 1,
                                                "tamanhoPagina": 20,
                                                "quantidadeElementos": 1,
                                                "primeiraPagina": true,
                                                "ultimaPagina": true,
                                                "vazia": false
                                              },
                                              "mensagem": "Registros operacionais de webhooks encontrados"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Filtros, paginação ou ordenação inválidos
                            """,
                    content = @Content(
                            mediaType =
                                    MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "periodoInvalido",
                                            summary = """
                                                    Data inicial posterior
                                                    à data final
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Data inicial não pode ser posterior à data final"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "tamanhoPaginaInvalido",
                                            summary = """
                                                    Tamanho da página acima
                                                    do limite
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Tamanho máximo da página é 100"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "ordenacaoInvalida",
                                            summary = """
                                                    Campo não permitido para
                                                    ordenação
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Campo de ordenação inválido: payloadOriginal"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            Token não enviado, inválido, expirado ou
                            bloqueado
                            """,
                    content = @Content(
                            mediaType =
                                    MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "naoAutenticado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Token não enviado, inválido, expirado ou bloqueado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            Usuário autenticado não possui perfil ADMIN
                            """,
                    content = @Content(
                            mediaType =
                                    MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "acessoNegado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Acesso negado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = """
                            Limite temporário de requisições excedido
                            """,
                    content = @Content(
                            mediaType =
                                    MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "muitasRequisicoes",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Muitas requisições. Aguarde um momento."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                            Falha interna ou indisponibilidade da consulta
                            operacional
                            """,
                    content = @Content(
                            mediaType =
                                    MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "erroInterno",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Erro interno no servidor"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public RespostaApi<
            PaginaResponseDTO<
                    RegistroOperacionalWebhookPagamentoResponseDTO
                    >
            > consultar(
            @Parameter(hidden = true)
            @Valid
            @ModelAttribute
            RegistroOperacionalWebhookPagamentoFiltroDTO filtro,

            @Parameter(hidden = true)
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "dataRecebimento",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return RespostaApi.sucesso(
                consultaService.consultar(
                        filtro,
                        pageable
                ),
                "Registros operacionais de webhooks encontrados"
        );
    }
}