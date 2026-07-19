package br.com.api.pedidos.coupon.controller;

import br.com.api.pedidos.coupon.dto.CupomRequestDTO;
import br.com.api.pedidos.coupon.dto.CupomResponseDTO;
import br.com.api.pedidos.coupon.dto.openapi.RespostaCupomOpenApiDTO;
import br.com.api.pedidos.coupon.dto.openapi.RespostaPaginaCuponsOpenApiDTO;
import br.com.api.pedidos.coupon.service.CupomService;
import br.com.api.pedidos.shared.idempotency.service.IdempotencyService;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroOpenApiDTO;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroValidacaoOpenApiDTO;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.response.RespostaApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Cupons",
        description = """
                Cadastro, consulta e gerenciamento dos cupons de desconto.

                Todas as operações deste controller são exclusivas
                para administradores.
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
                responseCode = "403",
                description = "Usuário autenticado não possui perfil ADMIN",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(
                                implementation =
                                        RespostaErroOpenApiDTO.class
                        ),
                        examples = @ExampleObject(
                                name = "acessoNegado",
                                summary = "Usuário sem permissão administrativa",
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
@RequestMapping("/cupons")
@PreAuthorize("hasRole('ADMIN')")
public class CupomController {

    private final CupomService cupomService;
    private final IdempotencyService idempotencyService;

    public CupomController(
            CupomService cupomService,
            IdempotencyService idempotencyService) {
        this.cupomService = cupomService;
        this.idempotencyService = idempotencyService;
    }

    @Operation(
            summary = "Cadastrar cupom",
            description = """
                    Cadastra um novo cupom de desconto ativo.

                    O percentual deve ser informado em formato decimal.
                    Por exemplo, 0.15 representa desconto de 15%.

                    A data final precisa estar no futuro e não pode ser
                    anterior à data inicial.

                    A operação exige o header Idempotency-Key.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = """
                            Cupom criado ou resposta idempotente recuperada
                            com sucesso
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaCupomOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "cupomCriado",
                                            summary = "Cupom criado",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "id": 5,
                                                        "codigo": "DESCONTO15",
                                                        "percentual": 0.15,
                                                        "dataInicio": "2026-08-01T00:00:00",
                                                        "dataFim": "2026-12-31T23:59:59",
                                                        "ativo": true,
                                                        "limiteUso": 100,
                                                        "quantidadeDeUso": 0
                                                      },
                                                      "mensagem": "Cupom criado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "respostaIdempotente",
                                            summary = """
                                                    Cupom já criado anteriormente
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "id": 5,
                                                        "codigo": "DESCONTO15",
                                                        "percentual": 0.15,
                                                        "dataInicio": "2026-08-01T00:00:00",
                                                        "dataFim": "2026-12-31T23:59:59",
                                                        "ativo": true,
                                                        "limiteUso": 100,
                                                        "quantidadeDeUso": 0
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
                            Dados do cupom, JSON ou Idempotency-Key inválidos,
                            ou código de cupom já cadastrado
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
                                            name = "dadosCupomInvalidos",
                                            summary = "Campos do cupom inválidos",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": {
                                                        "codigo": "Código do cupom é obrigatório",
                                                        "percentual": "Percentual deve ser maior que zero",
                                                        "dataInicio": "Data de início é obrigatória",
                                                        "dataFim": "Data de fim deve ser futura",
                                                        "limiteUso": "Limite de uso deve ser maior que zero"
                                                      },
                                                      "mensagem": "Dados inválidos"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "codigoCupomJaExiste",
                                            summary = "Código já cadastrado",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Código de cupom já existe"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "periodoCupomInvalido",
                                            summary = """
                                                    Data final anterior à data inicial
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Data final não pode ser anterior à data inicial"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "jsonInvalido",
                                            summary = "JSON inválido",
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
                    responseCode = "409",
                    description = """
                            Chave idempotente reutilizada com outro conteúdo
                            ou conflito de integridade
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "chaveReutilizada",
                                            summary = """
                                                    Idempotency-Key reutilizada
                                                    com outro corpo
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Idempotency-Key reutilizada com payload diferente"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "violacaoIntegridade",
                                            summary = "Conflito de integridade",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Violação de integridade dos dados"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaApi<CupomResponseDTO> criarCupom(
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
                            "cupom-550e8400-e29b-41d4-a716-446655440000",
                    schema = @Schema(
                            type = "string",
                            maxLength = 255
                    )
            )
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody CupomRequestDTO cupomRequestDTO,
            HttpServletRequest request) {
        return idempotencyService.executar(
                key,
                request,
                cupomRequestDTO,
                CupomResponseDTO.class,
                () -> cupomService.criarCupom(cupomRequestDTO),
                "Cupom criado com sucesso"
        );
    }

    @Operation(
            summary = "Listar cupons",
            description = """
                    Retorna todos os cupons de forma paginada.

                    Campos permitidos para ordenação:

                    id, codigo, percentual, dataInicio, dataFim, ativo,
                    limiteUso e quantidadeDeUso.

                    A ordenação padrão é dataFim,desc.
                    """,
            parameters = {
                    @Parameter(
                            name = "page",
                            description = """
                                    Número da página consultada.

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
                                    Quantidade de cupons por página.

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

                                    Direções permitidas: asc e desc.

                                    Campos permitidos:
                                    id, codigo, percentual, dataInicio,
                                    dataFim, ativo, limiteUso e quantidadeDeUso.

                                    É possível enviar mais de um parâmetro sort.

                                    Exemplos:
                                    sort=dataFim,desc
                                    sort=ativo,desc&sort=dataFim,asc
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "dataFim,desc",
                            array = @ArraySchema(
                                    arraySchema = @Schema(
                                            defaultValue = "dataFim,desc"
                                    ),
                                    schema = @Schema(
                                            type = "string",
                                            example = "dataFim,desc"
                                    )
                            )
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cupons encontrados",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPaginaCuponsOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "cuponsEncontrados",
                                    summary = "Página de cupons encontrada",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "conteudo": [
                                                  {
                                                    "id": 5,
                                                    "codigo": "DESCONTO15",
                                                    "percentual": 0.15,
                                                    "dataInicio": "2026-08-01T00:00:00",
                                                    "dataFim": "2026-12-31T23:59:59",
                                                    "ativo": true,
                                                    "limiteUso": 100,
                                                    "quantidadeDeUso": 12
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
                                              "mensagem": "Cupons encontrados"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros de paginação inválidos",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "tamanhoPaginaInvalido",
                                            summary = """
                                                    Tamanho da página menor
                                                    ou igual a zero
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Tamanho da página deve ser maior que zero"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "tamanhoPaginaExcedido",
                                            summary = "Tamanho máximo excedido",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Tamanho máximo da página é 100"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "parametroPaginaInvalido",
                                            summary = """
                                                    Tipo do parâmetro incompatível
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Parâmetro inválido: page"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @GetMapping
    public RespostaApi<PaginaResponseDTO<CupomResponseDTO>> listarCupons(
            @Parameter(hidden = true)
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "dataFim",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return RespostaApi.sucesso(
                cupomService.listarCupons(pageable),
                "Cupons encontrados"
        );
    }

    @Operation(
            summary = "Buscar cupom por ID",
            description = """
                    Retorna os dados de um cupom pelo seu identificador.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cupom encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaCupomOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "cupomEncontrado",
                                    summary = "Cupom encontrado",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "id": 5,
                                                "codigo": "DESCONTO15",
                                                "percentual": 0.15,
                                                "dataInicio": "2026-08-01T00:00:00",
                                                "dataFim": "2026-12-31T23:59:59",
                                                "ativo": true,
                                                "limiteUso": 100,
                                                "quantidadeDeUso": 12
                                              },
                                              "mensagem": "Cupom encontrado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identificador informado em formato inválido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "idCupomInvalido",
                                    summary = "Identificador incompatível",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Parâmetro inválido: id"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cupom não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "cupomNaoEncontrado",
                                    summary = "Cupom inexistente",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Cupom não encontrado"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public RespostaApi<CupomResponseDTO> buscarCupomPorId(
            @Parameter(
                    description = "Identificador do cupom",
                    required = true,
                    example = "5"
            )
            @PathVariable Long id) {
        return RespostaApi.sucesso(
                cupomService.buscarCupomPorId(id),
                "Cupom encontrado"
        );
    }

    @Operation(
            summary = "Ativar cupom",
            description = """
                    Ativa um cupom que esteja atualmente desativado.

                    A operação exige o header Idempotency-Key.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Cupom ativado ou resposta idempotente recuperada
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaCupomOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "cupomAtivado",
                                            summary = "Cupom ativado",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "id": 5,
                                                        "codigo": "DESCONTO15",
                                                        "percentual": 0.15,
                                                        "dataInicio": "2026-08-01T00:00:00",
                                                        "dataFim": "2026-12-31T23:59:59",
                                                        "ativo": true,
                                                        "limiteUso": 100,
                                                        "quantidadeDeUso": 12
                                                      },
                                                      "mensagem": "Cupom ativado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "ativacaoJaProcessada",
                                            summary = """
                                                    Ativação já processada anteriormente
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "id": 5,
                                                        "codigo": "DESCONTO15",
                                                        "percentual": 0.15,
                                                        "dataInicio": "2026-08-01T00:00:00",
                                                        "dataFim": "2026-12-31T23:59:59",
                                                        "ativo": true,
                                                        "limiteUso": 100,
                                                        "quantidadeDeUso": 12
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
                            Identificador ou Idempotency-Key inválida
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "idCupomInvalido",
                                            summary = "Identificador incompatível",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Parâmetro inválido: id"
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
                    description = "Cupom não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "cupomNaoEncontrado",
                                    summary = "Cupom inexistente",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Cupom não encontrado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cupom já está ativo",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "cupomJaAtivo",
                                    summary = "Cupom já se encontra ativo",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Cupom já está ativo"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{id}/ativar")
    public RespostaApi<CupomResponseDTO> ativarCupom(
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
                            "ativar-cupom-550e8400-e29b-41d4-a716-446655440000",
                    schema = @Schema(
                            type = "string",
                            maxLength = 255
                    )
            )
            @RequestHeader("Idempotency-Key")
            String key,

            HttpServletRequest request,

            @Parameter(
                    description = "Identificador do cupom",
                    required = true,
                    example = "5"
            )
            @PathVariable Long id) {
        return idempotencyService.executar(
                key,
                request,
                null,
                CupomResponseDTO.class,
                () -> cupomService.ativarCupom(id),
                "Cupom ativado com sucesso"
        );
    }

    @Operation(
            summary = "Desativar cupom",
            description = """
                    Desativa um cupom que esteja atualmente ativo.

                    A operação exige o header Idempotency-Key.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Cupom desativado ou resposta idempotente recuperada
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaCupomOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "cupomDesativado",
                                            summary = "Cupom desativado",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "id": 5,
                                                        "codigo": "DESCONTO15",
                                                        "percentual": 0.15,
                                                        "dataInicio": "2026-08-01T00:00:00",
                                                        "dataFim": "2026-12-31T23:59:59",
                                                        "ativo": false,
                                                        "limiteUso": 100,
                                                        "quantidadeDeUso": 12
                                                      },
                                                      "mensagem": "Cupom desativado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "desativacaoJaProcessada",
                                            summary = """
                                                    Desativação já processada anteriormente
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "id": 5,
                                                        "codigo": "DESCONTO15",
                                                        "percentual": 0.15,
                                                        "dataInicio": "2026-08-01T00:00:00",
                                                        "dataFim": "2026-12-31T23:59:59",
                                                        "ativo": false,
                                                        "limiteUso": 100,
                                                        "quantidadeDeUso": 12
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
                            Identificador ou Idempotency-Key inválida
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "idCupomInvalido",
                                            summary = "Identificador incompatível",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Parâmetro inválido: id"
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
                    description = "Cupom não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "cupomNaoEncontrado",
                                    summary = "Cupom inexistente",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Cupom não encontrado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cupom já está desativado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "cupomJaDesativado",
                                    summary = "Cupom já se encontra desativado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Cupom já está desativado"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{id}/desativar")
    public RespostaApi<CupomResponseDTO> desativarCupom(
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
                            "desativar-cupom-550e8400-e29b-41d4-a716-446655440000",
                    schema = @Schema(
                            type = "string",
                            maxLength = 255
                    )
            )
            @RequestHeader("Idempotency-Key")
            String key,

            HttpServletRequest request,

            @Parameter(
                    description = "Identificador do cupom",
                    required = true,
                    example = "5"
            )
            @PathVariable Long id) {
        return idempotencyService.executar(
                key,
                request,
                null,
                CupomResponseDTO.class,
                () -> cupomService.desativarCupom(id),
                "Cupom desativado com sucesso"
        );
    }
}
