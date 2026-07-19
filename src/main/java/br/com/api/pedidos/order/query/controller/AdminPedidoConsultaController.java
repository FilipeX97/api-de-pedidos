package br.com.api.pedidos.order.query.controller;

import br.com.api.pedidos.order.query.dto.PedidoFiltroDTO;
import br.com.api.pedidos.order.query.dto.PedidoResumoResponseDTO;
import br.com.api.pedidos.order.query.dto.openapi.RespostaPaginaPedidosAdministrativosOpenApiDTO;
import br.com.api.pedidos.order.query.service.PedidoConsultaService;
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
        name = "Consulta administrativa de pedidos",
        description = """
                Consulta paginada de todos os pedidos cadastrados na API.

                O administrador pode combinar filtros por cliente, status,
                período, valores e cupom.

                Todas as operações deste controller são exclusivas para
                usuários com perfil ADMIN.
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
                                                IP ou User-Agent diferente
                                                do token
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
                                                Token não pertence ao usuário
                                                carregado
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
                                        summary = """
                                                Usuário do token está desativado
                                                """,
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
                                                Senha alterada após a emissão
                                                do token
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
                description = """
                        Usuário autenticado não possui perfil ADMIN
                        """,
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(
                                implementation =
                                        RespostaErroOpenApiDTO.class
                        ),
                        examples = @ExampleObject(
                                name = "acessoNegado",
                                summary = """
                                        Usuário sem permissão administrativa
                                        """,
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
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(
                                implementation =
                                        RespostaErroOpenApiDTO.class
                        ),
                        examples = @ExampleObject(
                                name = "muitasRequisicoes",
                                summary = """
                                        Limite de requisições excedido
                                        """,
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
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPedidoConsultaController {

    private final PedidoConsultaService pedidoConsultaService;

    public AdminPedidoConsultaController(
            PedidoConsultaService pedidoConsultaService
    ) {
        this.pedidoConsultaService = pedidoConsultaService;
    }

    @Operation(
            summary = "Consultar pedidos administrativamente",
            description = """
                    Retorna todos os pedidos da API de forma paginada.

                    Todos os filtros são opcionais e podem ser combinados.

                    O filtro de e-mail realiza uma comparação exata,
                    ignorando diferenças entre letras maiúsculas e
                    minúsculas.

                    As datas inicial e final são inclusivas.

                    Os filtros valorMinimo e valorMaximo são aplicados
                    sobre o valor final do pedido.

                    O código do cupom também utiliza comparação exata,
                    ignorando diferenças entre letras maiúsculas e
                    minúsculas.

                    Campos permitidos para ordenação:

                    idPedido, nomeCliente, emailCliente, status,
                    valorBruto, valorDesconto, valorFinal, codigoCupom
                    e dataCriacao.

                    A ordenação padrão é dataCriacao,desc.
                    """,
            parameters = {
                    @Parameter(
                            name = "status",
                            description = "Status atual do pedido",
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "PAGO",
                            schema = @Schema(
                                    type = "string",
                                    allowableValues = {
                                            "CRIADO",
                                            "AGUARDANDO_PAGAMENTO",
                                            "PAGO",
                                            "ENVIADO",
                                            "ENTREGUE",
                                            "CANCELAMENTO_SOLICITADO",
                                            "ESTORNADO",
                                            "CANCELADO"
                                    }
                            )
                    ),
                    @Parameter(
                            name = "idUsuario",
                            description = """
                                    Identificador do usuário proprietário
                                    do pedido
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "10",
                            schema = @Schema(
                                    type = "integer",
                                    format = "int64",
                                    minimum = "1"
                            )
                    ),
                    @Parameter(
                            name = "emailCliente",
                            description = """
                                    E-mail exato do cliente.

                                    A comparação ignora letras maiúsculas
                                    e minúsculas.
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "cliente@exemplo.com",
                            schema = @Schema(
                                    type = "string",
                                    format = "email",
                                    maxLength = 255
                            )
                    ),
                    @Parameter(
                            name = "dataInicio",
                            description = """
                                    Data inicial inclusiva do período de
                                    criação
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "2026-07-01",
                            schema = @Schema(
                                    type = "string",
                                    format = "date"
                            )
                    ),
                    @Parameter(
                            name = "dataFim",
                            description = """
                                    Data final inclusiva do período de
                                    criação
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "2026-07-31",
                            schema = @Schema(
                                    type = "string",
                                    format = "date"
                            )
                    ),
                    @Parameter(
                            name = "valorMinimo",
                            description = """
                                    Valor final mínimo do pedido
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "100.00",
                            schema = @Schema(
                                    type = "number",
                                    format = "decimal",
                                    minimum = "0.00"
                            )
                    ),
                    @Parameter(
                            name = "valorMaximo",
                            description = """
                                    Valor final máximo do pedido
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "1000.00",
                            schema = @Schema(
                                    type = "number",
                                    format = "decimal",
                                    minimum = "0.00"
                            )
                    ),
                    @Parameter(
                            name = "codigoCupom",
                            description = """
                                    Código exato do cupom aplicado ao pedido.

                                    A comparação ignora letras maiúsculas
                                    e minúsculas.
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "DESCONTO15",
                            schema = @Schema(
                                    type = "string",
                                    maxLength = 255
                            )
                    ),
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
                                    Quantidade de pedidos por página.

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

                                    idPedido,
                                    nomeCliente,
                                    emailCliente,
                                    status,
                                    valorBruto,
                                    valorDesconto,
                                    valorFinal,
                                    codigoCupom e
                                    dataCriacao.

                                    É possível enviar mais de um parâmetro
                                    sort.

                                    Exemplos:

                                    sort=dataCriacao,desc

                                    sort=status,asc&sort=dataCriacao,desc
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "dataCriacao,desc",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            type = "string",
                                            example = "dataCriacao,desc"
                                    )
                            )
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedidos encontrados",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPaginaPedidosAdministrativosOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pedidosEncontrados",
                                            summary = """
                                                    Página de pedidos encontrada
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "conteudo": [
                                                          {
                                                            "idPedido": 150,
                                                            "nomeCliente": "João da Silva",
                                                            "emailCliente": "joao.silva@exemplo.com",
                                                            "status": "PAGO",
                                                            "valorBruto": 500.00,
                                                            "valorDesconto": 75.00,
                                                            "valorFinal": 425.00,
                                                            "codigoCupom": "DESCONTO15",
                                                            "dataCriacao": "2026-07-19T14:30:00",
                                                            "quantidadeItens": 3
                                                          },
                                                          {
                                                            "idPedido": 149,
                                                            "nomeCliente": "Maria Souza",
                                                            "emailCliente": "maria.souza@exemplo.com",
                                                            "status": "CRIADO",
                                                            "valorBruto": 200.00,
                                                            "valorDesconto": 0.00,
                                                            "valorFinal": 200.00,
                                                            "codigoCupom": null,
                                                            "dataCriacao": "2026-07-19T14:00:00",
                                                            "quantidadeItens": 1
                                                          }
                                                        ],
                                                        "paginaAtual": 0,
                                                        "totalPaginas": 8,
                                                        "totalElementos": 145,
                                                        "tamanhoPagina": 20,
                                                        "quantidadeElementos": 20,
                                                        "primeiraPagina": true,
                                                        "ultimaPagina": false,
                                                        "vazia": false
                                                      },
                                                      "mensagem": "Pedidos encontrados"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "nenhumPedidoEncontrado",
                                            summary = """
                                                    Nenhum pedido corresponde
                                                    aos filtros
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "conteudo": [],
                                                        "paginaAtual": 0,
                                                        "totalPaginas": 0,
                                                        "totalElementos": 0,
                                                        "tamanhoPagina": 20,
                                                        "quantidadeElementos": 0,
                                                        "primeiraPagina": true,
                                                        "ultimaPagina": true,
                                                        "vazia": true
                                                      },
                                                      "mensagem": "Pedidos encontrados"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Filtros, paginação ou ordenação inválidos
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
                                            name = "camposFiltroInvalidos",
                                            summary = """
                                                    Campos do filtro não
                                                    passaram pela validação
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": {
                                                        "idUsuario": "Id do usuário deve ser maior que zero",
                                                        "emailCliente": "E-mail do cliente inválido",
                                                        "valorMinimo": "Valor mínimo não pode ser negativo",
                                                        "valorMaximo": "Valor máximo não pode ser negativo"
                                                      },
                                                      "mensagem": "Dados inválidos"
                                                    }
                                                    """
                                    ),
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
                                            name = "intervaloValoresInvalido",
                                            summary = """
                                                    Valor mínimo maior que
                                                    o valor máximo
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Valor mínimo não pode ser maior que o valor máximo"
                                                    }
                                                    """
                                    ),
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
                                            summary = """
                                                    Tamanho máximo da página
                                                    excedido
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
                                            name = "parametroPaginaInvalido",
                                            summary = """
                                                    Número da página em formato
                                                    incompatível
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Parâmetro inválido: page"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "campoOrdenacaoInvalido",
                                            summary = """
                                                    Campo não permitido para
                                                    ordenação
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Campo de ordenação inválido: senha. Campos permitidos: [idPedido, nomeCliente, emailCliente, status, valorBruto, valorDesconto, valorFinal, codigoCupom, dataCriacao]"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @GetMapping
    public RespostaApi<PaginaResponseDTO<PedidoResumoResponseDTO>> consultarPedido(
            @Parameter(hidden = true)
            @Valid @ModelAttribute PedidoFiltroDTO pedidoFiltroDTO,

            @Parameter(hidden = true)
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "dataCriacao",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
            ) {
        return RespostaApi.sucesso(
                pedidoConsultaService.consultar(
                        pedidoFiltroDTO,
                        pageable
                ),
                "Pedidos encontrados"
        );
    }

}
