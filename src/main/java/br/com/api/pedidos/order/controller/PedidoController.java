package br.com.api.pedidos.order.controller;

import br.com.api.pedidos.order.dto.AdicionarPedidoRequestDTO;
import br.com.api.pedidos.order.dto.AlterarQuantidadeItemRequestDTO;
import br.com.api.pedidos.order.dto.AplicarCupomRequestDTO;
import br.com.api.pedidos.order.dto.PedidoResponseDTO;
import br.com.api.pedidos.order.dto.openapi.RespostaPaginaPedidosUsuarioOpenApiDTO;
import br.com.api.pedidos.order.dto.openapi.RespostaPedidoOpenApiDTO;
import br.com.api.pedidos.order.query.dto.PedidoUsuarioResumoResponseDTO;
import br.com.api.pedidos.order.query.service.PedidoUsuarioConsultaService;
import br.com.api.pedidos.order.service.PedidoService;
import br.com.api.pedidos.security.service.UsuarioLogadoService;
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
        name = "Pedidos",
        description = """
                Criação e gerenciamento dos pedidos do usuário autenticado.

                Usuários comuns podem criar pedidos, consultar os próprios
                pedidos, gerenciar itens, aplicar cupons e solicitar
                cancelamentos.

                O envio, a entrega e o estorno são operações exclusivas
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
@RequestMapping("/orders")
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioLogadoService usuarioLogadoService;
    private final IdempotencyService idempotencyService;
    private final PedidoUsuarioConsultaService pedidoUsuarioConsultaService;

    public PedidoController(
            PedidoService pedidoService,
            UsuarioLogadoService usuarioLogadoService,
            IdempotencyService idempotencyService,
            PedidoUsuarioConsultaService pedidoUsuarioConsultaService
    ) {
        this.pedidoService = pedidoService;
        this.usuarioLogadoService = usuarioLogadoService;
        this.idempotencyService = idempotencyService;
        this.pedidoUsuarioConsultaService = pedidoUsuarioConsultaService;
    }

    @Operation(
            summary = "Listar meus pedidos",
            description = """
                    Retorna de forma paginada somente os pedidos pertencentes
                    ao usuário autenticado.

                    Campos permitidos para ordenação:

                    idPedido, dataCriacao, status, valorBruto,
                    valorDesconto, valorFinal e codigoCupom.

                    A ordenação padrão é dataCriacao,desc.
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
                                    idPedido, dataCriacao, status, valorBruto,
                                    valorDesconto, valorFinal e codigoCupom.

                                    É possível enviar mais de um parâmetro sort.

                                    Exemplos:
                                    sort=dataCriacao,desc
                                    sort=status,asc&sort=dataCriacao,desc
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "dataCriacao,desc",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            type = "string"
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
                                            RespostaPaginaPedidosUsuarioOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidosEncontrados",
                                    summary = "Página de pedidos encontrada",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "conteudo": [
                                                  {
                                                    "idPedido": 10,
                                                    "dataCriacao": "2026-07-18T14:30:00",
                                                    "status": "CRIADO",
                                                    "valorBruto": 300.00,
                                                    "valorDesconto": 30.00,
                                                    "valorFinal": 270.00,
                                                    "codigoCupom": "DESCONTO10",
                                                    "quantidadeItens": 2
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
                                              "mensagem": "Pedidos encontrados"
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
                                            summary = "Tipo do parâmetro inválido",
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
    public RespostaApi<PaginaResponseDTO<PedidoUsuarioResumoResponseDTO>> buscarTodosPedidos(
            @Parameter(hidden = true)
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "dataCriacao",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                pedidoUsuarioConsultaService.listarPedidosDoUsuario(
                        usuario,
                        pageable
                ),
                "Pedidos encontrados"
        );
    }

    @Operation(
            summary = "Buscar pedido por ID",
            description = """
                    Retorna os dados detalhados de um pedido pertencente
                    ao usuário autenticado.

                    Caso o pedido pertença a outro usuário, a API retorna
                    a mesma resposta utilizada para pedido inexistente.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPedidoOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoEncontrado",
                                    summary = "Pedido encontrado",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "idPedido": 10,
                                                "idUsuario": 2,
                                                "dataCriacao": "2026-07-18T14:30:00",
                                                "status": "CRIADO",
                                                "valorBruto": 300.00,
                                                "valorDesconto": 0.00,
                                                "valorFinal": 300.00,
                                                "codigoCupom": null,
                                                "itens": [
                                                  {
                                                    "itemPedidoId": 21,
                                                    "produtoId": 5,
                                                    "nomeProduto": "Teclado Mecânico",
                                                    "quantidade": 2,
                                                    "precoUnitario": 150.00,
                                                    "subtotal": 300.00
                                                  }
                                                ]
                                              },
                                              "mensagem": "Pedido encontrado"
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
                                    name = "idPedidoInvalido",
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
                    description = "Pedido não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoEncontrado",
                                    summary = "Pedido inexistente ou de outro usuário",
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
    @GetMapping("/{id}")
    public RespostaApi<PedidoResponseDTO> buscarPedidoPorId(
            @Parameter(
                    description = "Identificador do pedido",
                    required = true,
                    example = "10"
            )
            @PathVariable Long id) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                pedidoService.buscarPedidoPorId(id, usuario),
                "Pedido encontrado"
        );
    }

    @Operation(
            summary = "Criar pedido",
            description = """
                    Cria um pedido vazio para o usuário autenticado.

                    A operação exige o header Idempotency-Key.

                    Repetir a mesma chave para esta operação retorna o
                    pedido anteriormente criado.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido criado ou resposta idempotente recuperada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPedidoOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pedidoCriado",
                                            summary = "Pedido criado",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "CRIADO",
                                                        "valorBruto": 0,
                                                        "valorDesconto": 0,
                                                        "valorFinal": 0,
                                                        "codigoCupom": null,
                                                        "itens": []
                                                      },
                                                      "mensagem": "Pedido criado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "pedidoJaProcessado",
                                            summary = "Resposta idempotente recuperada",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "CRIADO",
                                                        "valorBruto": 0,
                                                        "valorDesconto": 0,
                                                        "valorFinal": 0,
                                                        "codigoCupom": null,
                                                        "itens": []
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
                    description = "Idempotency-Key ausente ou inválida",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "headerIdempotenciaAusente",
                                            summary = "Header obrigatório não enviado",
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
                                            summary = "Chave vazia",
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
                                            summary = "Chave maior que 255 caracteres",
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
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaApi<PedidoResponseDTO> criarPedido(
            @Parameter(
                    name = "Idempotency-Key",
                    description = """
                            Chave única da operação.

                            Pode possuir no máximo 255 caracteres e
                            permanece válida por 24 horas.
                            """,
                    in = ParameterIn.HEADER,
                    required = true,
                    example =
                            "pedido-550e8400-e29b-41d4-a716-446655440000",
                    schema = @Schema(
                            type = "string",
                            maxLength = 255
                    )
            )
            @RequestHeader("Idempotency-Key") String key,
            HttpServletRequest request) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return idempotencyService.executar(
                key,
                request,
                null,
                PedidoResponseDTO.class,
                () -> pedidoService.criarPedido(usuario),
                "Pedido criado com sucesso"
        );
    }

    @Operation(
            summary = "Adicionar item ao pedido",
            description = """
                    Adiciona um produto ao pedido do usuário autenticado.

                    O pedido precisa permitir alteração de itens e o produto
                    precisa possuir estoque suficiente.

                    A operação exige o header Idempotency-Key.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Item adicionado ou resposta idempotente recuperada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPedidoOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "itemAdicionado",
                                            summary = "Item adicionado ao pedido",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "CRIADO",
                                                        "valorBruto": 300.00,
                                                        "valorDesconto": 0.00,
                                                        "valorFinal": 300.00,
                                                        "codigoCupom": null,
                                                        "itens": [
                                                          {
                                                            "itemPedidoId": 21,
                                                            "produtoId": 5,
                                                            "nomeProduto": "Teclado Mecânico",
                                                            "quantidade": 2,
                                                            "precoUnitario": 150.00,
                                                            "subtotal": 300.00
                                                          }
                                                        ]
                                                      },
                                                      "mensagem": "Item adicionado ao pedido com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "itemJaProcessado",
                                            summary = "Resposta idempotente recuperada",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "CRIADO",
                                                        "valorBruto": 300.00,
                                                        "valorDesconto": 0.00,
                                                        "valorFinal": 300.00,
                                                        "codigoCupom": null,
                                                        "itens": [
                                                          {
                                                            "itemPedidoId": 21,
                                                            "produtoId": 5,
                                                            "nomeProduto": "Teclado Mecânico",
                                                            "quantidade": 2,
                                                            "precoUnitario": 150.00,
                                                            "subtotal": 300.00
                                                          }
                                                        ]
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
                            Dados inválidos, estoque insuficiente,
                            JSON inválido ou Idempotency-Key inválida
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
                                            name = "dadosItemInvalidos",
                                            summary = "Dados do item inválidos",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": {
                                                        "idProduto": "Id do produto é obrigatório",
                                                        "quantidade": "Quantidade deve ser maior que zero"
                                                      },
                                                      "mensagem": "Dados inválidos"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "estoqueInsuficiente",
                                            summary = "Produto sem estoque suficiente",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Estoque insuficiente"
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
                                            summary = "Header obrigatório não enviado",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Header obrigatório não enviado: Idempotency-Key"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido ou produto não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pedidoNaoEncontrado",
                                            summary = "Pedido inexistente",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Pedido não encontrado"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "produtoNaoEncontrado",
                                            summary = "Produto inexistente",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Produto não encontrado"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                            Estado do pedido não permite alteração ou chave
                            idempotente reutilizada com outro conteúdo
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "statusNaoPermiteAlterarItens",
                                            summary = "Estado incompatível",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Pedido com status PAGO não permite alterar itens."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "chaveReutilizada",
                                            summary = "Chave reutilizada com outro payload",
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
    @PostMapping("/{idPedido}/items")
    public RespostaApi<PedidoResponseDTO> adicionarItemPedido(
            @Parameter(
                    name = "Idempotency-Key",
                    description = """
                            Chave única da operação.

                            Pode possuir no máximo 255 caracteres e
                            permanece válida por 24 horas.
                            """,
                    in = ParameterIn.HEADER,
                    required = true,
                    example =
                            "item-550e8400-e29b-41d4-a716-446655440000",
                    schema = @Schema(
                            type = "string",
                            maxLength = 255
                    )
            )
            @RequestHeader("Idempotency-Key") String key,

            HttpServletRequest request,

            @Parameter(
                    description = "Identificador do pedido",
                    required = true,
                    example = "10"
            )
            @PathVariable Long idPedido,
            @Valid @RequestBody AdicionarPedidoRequestDTO adicionarPedidoRequestDTO) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return idempotencyService.executar(
                key,
                request,
                adicionarPedidoRequestDTO,
                PedidoResponseDTO.class,
                () -> pedidoService.adicionarItemPedido(
                        idPedido,
                        adicionarPedidoRequestDTO,
                        usuario),
                "Item adicionado ao pedido com sucesso"
        );
    }

    @Operation(
            summary = "Alterar quantidade de um item",
            description = """
                    Altera a quantidade de um item já existente no pedido.

                    O pedido precisa pertencer ao usuário autenticado e seu
                    estado atual precisa permitir alteração de itens.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Quantidade alterada com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPedidoOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "quantidadeAlterada",
                                    summary = "Quantidade do item alterada",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "idPedido": 10,
                                                "idUsuario": 2,
                                                "dataCriacao": "2026-07-18T14:30:00",
                                                "status": "CRIADO",
                                                "valorBruto": 450.00,
                                                "valorDesconto": 0.00,
                                                "valorFinal": 450.00,
                                                "codigoCupom": null,
                                                "itens": [
                                                  {
                                                    "itemPedidoId": 21,
                                                    "produtoId": 5,
                                                    "nomeProduto": "Teclado Mecânico",
                                                    "quantidade": 3,
                                                    "precoUnitario": 150.00,
                                                    "subtotal": 450.00
                                                  }
                                                ]
                                              },
                                              "mensagem": "Quantidade alterada com sucesso"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Quantidade, item ou JSON inválido",
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
                                            name = "novaQuantidadeInvalida",
                                            summary = "Nova quantidade inválida",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": {
                                                        "novaQuantidade": "Nova quantidade deve ser maior que zero"
                                                      },
                                                      "mensagem": "Dados inválidos"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "idItemInvalido",
                                            summary = "Identificador do item inválido",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Id do item do pedido inválido"
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
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido ou item não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pedidoNaoEncontrado",
                                            summary = "Pedido inexistente",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Pedido não encontrado"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "itemNaoEncontrado",
                                            summary = "Item não pertence ao pedido",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Item não encontrado no pedido"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Estado atual do pedido não permite a operação",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "statusNaoPermiteAlterarItens",
                                            summary = "Estado incompatível",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Pedido com status PAGO não permite alterar itens."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "pedidoSemItens",
                                            summary = "Pedido não possui itens",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Não há itens no pedido"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PatchMapping("/{idPedido}/items/{itemId}")
    public RespostaApi<PedidoResponseDTO> alterarQuantidadeItemPedido(
            @Parameter(
                    description = "Identificador do pedido",
                    required = true,
                    example = "10"
            )
            @PathVariable Long idPedido,

            @Parameter(
                    description = "Identificador do item dentro do pedido",
                    required = true,
                    example = "21"
            )
            @PathVariable Long itemId,
            @Valid @RequestBody AlterarQuantidadeItemRequestDTO dto) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                pedidoService.alterarQuantidadeItemPedido(
                        idPedido,
                        itemId,
                        dto,
                        usuario
                ),
                "Quantidade alterada com sucesso"
        );
    }

    @Operation(
            summary = "Remover item do pedido",
            description = """
                    Remove completamente um item do pedido e devolve ao
                    produto a quantidade de estoque anteriormente reservada.

                    A operação exige o header Idempotency-Key.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Item removido ou resposta idempotente recuperada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPedidoOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "itemRemovido",
                                            summary = "Item removido",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "CRIADO",
                                                        "valorBruto": 0,
                                                        "valorDesconto": 0,
                                                        "valorFinal": 0,
                                                        "codigoCupom": null,
                                                        "itens": []
                                                      },
                                                      "mensagem": "Item removido do pedido com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "remocaoJaProcessada",
                                            summary = "Resposta idempotente recuperada",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "CRIADO",
                                                        "valorBruto": 0,
                                                        "valorDesconto": 0,
                                                        "valorFinal": 0,
                                                        "codigoCupom": null,
                                                        "itens": []
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
                    description = "Idempotency-Key ou identificador inválido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "headerIdempotenciaAusente",
                                            summary = "Header obrigatório não enviado",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Header obrigatório não enviado: Idempotency-Key"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "idItemInvalido",
                                            summary = "Identificador do item inválido",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Id do item do pedido inválido"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido ou item não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pedidoNaoEncontrado",
                                            summary = "Pedido inexistente",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Pedido não encontrado"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "itemNaoEncontrado",
                                            summary = "Item não pertence ao pedido",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Item não encontrado no pedido"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Estado atual do pedido não permite a operação",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "statusNaoPermiteAlterarItens",
                                            summary = "Estado incompatível",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Pedido com status PAGO não permite alterar itens."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "pedidoSemItens",
                                            summary = "Pedido não possui itens",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Não há itens no pedido"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @DeleteMapping("/{idPedido}/items/{itemId}")
    public RespostaApi<PedidoResponseDTO> removerItemPedido(
            @Parameter(
                    name = "Idempotency-Key",
                    description = """
                            Chave única da operação.

                            Pode possuir no máximo 255 caracteres e
                            permanece válida por 24 horas.
                            """,
                    in = ParameterIn.HEADER,
                    required = true,
                    example =
                            "remover-item-550e8400-e29b-41d4-a716-446655440000",
                    schema = @Schema(
                            type = "string",
                            maxLength = 255
                    )
            )
            @RequestHeader("Idempotency-Key") String key,
            HttpServletRequest request,

            @Parameter(
                    description = "Identificador do pedido",
                    required = true,
                    example = "10"
            )
            @PathVariable Long idPedido,

            @Parameter(
                    description = "Identificador do item dentro do pedido",
                    required = true,
                    example = "21"
            )
            @PathVariable Long itemId) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return idempotencyService.executar(
                key,
                request,
                null,
                PedidoResponseDTO.class,
                () -> pedidoService.removerItemPedido(
                        idPedido,
                        itemId,
                        usuario),
                "Item removido do pedido com sucesso"
        );
    }

    @Operation(
            summary = "Aplicar cupom ao pedido",
            description = """
                    Aplica um cupom válido ao pedido do usuário autenticado.

                    O pedido precisa permitir aplicação de cupom e o cupom
                    deve estar ativo, dentro do período de validade e abaixo
                    do limite de utilização.

                    A operação exige o header Idempotency-Key.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cupom aplicado ou resposta idempotente recuperada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPedidoOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "cupomAplicado",
                                            summary = "Cupom aplicado",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "CRIADO",
                                                        "valorBruto": 300.00,
                                                        "valorDesconto": 30.00,
                                                        "valorFinal": 270.00,
                                                        "codigoCupom": "DESCONTO10",
                                                        "itens": [
                                                          {
                                                            "itemPedidoId": 21,
                                                            "produtoId": 5,
                                                            "nomeProduto": "Teclado Mecânico",
                                                            "quantidade": 2,
                                                            "precoUnitario": 150.00,
                                                            "subtotal": 300.00
                                                          }
                                                        ]
                                                      },
                                                      "mensagem": "Cupom aplicado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "cupomJaProcessado",
                                            summary = "Resposta idempotente recuperada",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "CRIADO",
                                                        "valorBruto": 300.00,
                                                        "valorDesconto": 30.00,
                                                        "valorFinal": 270.00,
                                                        "codigoCupom": "DESCONTO10",
                                                        "itens": [
                                                          {
                                                            "itemPedidoId": 21,
                                                            "produtoId": 5,
                                                            "nomeProduto": "Teclado Mecânico",
                                                            "quantidade": 2,
                                                            "precoUnitario": 150.00,
                                                            "subtotal": 300.00
                                                          }
                                                        ]
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
                            Código do cupom, JSON ou Idempotency-Key inválida
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
                                            name = "codigoCupomAusente",
                                            summary = "Código não informado",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": {
                                                        "codigoCupom": "Código do cupom é obrigatório"
                                                      },
                                                      "mensagem": "Dados inválidos"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "cupomInvalido",
                                            summary = "Cupom inexistente",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Cupom inválido"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "cupomExpirado",
                                            summary = "Cupom indisponível",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Cupom expirado"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "headerIdempotenciaAusente",
                                            summary = "Header obrigatório não enviado",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Header obrigatório não enviado: Idempotency-Key"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoEncontrado",
                                    summary = "Pedido inexistente",
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
                            Estado do pedido não permite cupom ou chave
                            idempotente reutilizada com outro conteúdo
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "statusNaoPermiteAplicarCupom",
                                            summary = "Estado incompatível",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Pedido com status PAGO não permite aplicar cupom."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "chaveReutilizada",
                                            summary = "Chave reutilizada com outro payload",
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
    @PostMapping("/{idPedido}/coupon")
    public RespostaApi<PedidoResponseDTO> aplicarCupom(
            @Parameter(
                    name = "Idempotency-Key",
                    description = """
                            Chave única da operação.

                            Pode possuir no máximo 255 caracteres e
                            permanece válida por 24 horas.
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
            @RequestHeader("Idempotency-Key")
            String key,

            HttpServletRequest request,

            @Parameter(
                    description = "Identificador do pedido",
                    required = true,
                    example = "10"
            )
            @PathVariable Long idPedido,
            @Valid @RequestBody AplicarCupomRequestDTO dto) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return idempotencyService.executar(
                key,
                request,
                dto,
                PedidoResponseDTO.class,
                () -> pedidoService.aplicarCupom(
                        idPedido,
                        usuario,
                        dto.codigoCupom()),
                "Cupom aplicado com sucesso"
        );
    }

    @Operation(
            summary = "Enviar pedido",
            description = """
                    Altera o status de um pedido pago para ENVIADO.

                    Esta operação é exclusiva para administradores e exige
                    o header Idempotency-Key.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido enviado ou resposta idempotente recuperada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPedidoOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pedidoEnviado",
                                            summary = "Pedido enviado",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "ENVIADO",
                                                        "valorBruto": 300.00,
                                                        "valorDesconto": 0.00,
                                                        "valorFinal": 300.00,
                                                        "codigoCupom": null,
                                                        "itens": [
                                                          {
                                                            "itemPedidoId": 21,
                                                            "produtoId": 5,
                                                            "nomeProduto": "Teclado Mecânico",
                                                            "quantidade": 2,
                                                            "precoUnitario": 150.00,
                                                            "subtotal": 300.00
                                                          }
                                                        ]
                                                      },
                                                      "mensagem": "Pedido enviado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "envioJaProcessado",
                                            summary = "Resposta idempotente recuperada",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "ENVIADO",
                                                        "valorBruto": 300.00,
                                                        "valorDesconto": 0.00,
                                                        "valorFinal": 300.00,
                                                        "codigoCupom": null,
                                                        "itens": [
                                                          {
                                                            "itemPedidoId": 21,
                                                            "produtoId": 5,
                                                            "nomeProduto": "Teclado Mecânico",
                                                            "quantidade": 2,
                                                            "precoUnitario": 150.00,
                                                            "subtotal": 300.00
                                                          }
                                                        ]
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
                    description = "Idempotency-Key ausente ou inválida",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "headerIdempotenciaAusente",
                                    summary = "Header obrigatório não enviado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Header obrigatório não enviado: Idempotency-Key"
                                            }
                                            """
                            )
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
                    responseCode = "404",
                    description = "Pedido não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoEncontrado",
                                    summary = "Pedido inexistente",
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
                    description = "Estado atual não permite enviar o pedido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoPodeSerEnviado",
                                    summary = "Estado incompatível",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Pedido com status CRIADO não pode ser enviado."
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{idPedido}/ship")
    public RespostaApi<PedidoResponseDTO> enviarPedido(
            @Parameter(
                    name = "Idempotency-Key",
                    description = """
                            Chave única da operação.

                            Pode possuir no máximo 255 caracteres e
                            permanece válida por 24 horas.
                            """,
                    in = ParameterIn.HEADER,
                    required = true,
                    example =
                            "envio-550e8400-e29b-41d4-a716-446655440000",
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
            @PathVariable Long idPedido) {
        return idempotencyService.executar(
                key,
                request,
                null,
                PedidoResponseDTO.class,
                () -> pedidoService.enviarPedido(idPedido),
                "Pedido enviado com sucesso"
        );
    }

    @Operation(
            summary = "Entregar pedido",
            description = """
                    Altera o status de um pedido enviado para ENTREGUE.

                    Esta operação é exclusiva para administradores e exige
                    o header Idempotency-Key.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido entregue ou resposta idempotente recuperada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPedidoOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pedidoEntregue",
                                            summary = "Pedido entregue",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "ENTREGUE",
                                                        "valorBruto": 300.00,
                                                        "valorDesconto": 0.00,
                                                        "valorFinal": 300.00,
                                                        "codigoCupom": null,
                                                        "itens": [
                                                          {
                                                            "itemPedidoId": 21,
                                                            "produtoId": 5,
                                                            "nomeProduto": "Teclado Mecânico",
                                                            "quantidade": 2,
                                                            "precoUnitario": 150.00,
                                                            "subtotal": 300.00
                                                          }
                                                        ]
                                                      },
                                                      "mensagem": "Pedido entregue com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "entregaJaProcessada",
                                            summary = "Resposta idempotente recuperada",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "ENTREGUE",
                                                        "valorBruto": 300.00,
                                                        "valorDesconto": 0.00,
                                                        "valorFinal": 300.00,
                                                        "codigoCupom": null,
                                                        "itens": [
                                                          {
                                                            "itemPedidoId": 21,
                                                            "produtoId": 5,
                                                            "nomeProduto": "Teclado Mecânico",
                                                            "quantidade": 2,
                                                            "precoUnitario": 150.00,
                                                            "subtotal": 300.00
                                                          }
                                                        ]
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
                    description = "Idempotency-Key ausente ou inválida",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "headerIdempotenciaAusente",
                                    summary = "Header obrigatório não enviado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Header obrigatório não enviado: Idempotency-Key"
                                            }
                                            """
                            )
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
                    responseCode = "404",
                    description = "Pedido não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoEncontrado",
                                    summary = "Pedido inexistente",
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
                    description = "Estado atual não permite entregar o pedido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoPodeSerEntregue",
                                    summary = "Estado incompatível",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Pedido com status PAGO não pode ser entregue."
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{idPedido}/deliver")
    public RespostaApi<PedidoResponseDTO> entregarPedido(
            @Parameter(
                    name = "Idempotency-Key",
                    description = """
                            Chave única da operação.

                            Pode possuir no máximo 255 caracteres e
                            permanece válida por 24 horas.
                            """,
                    in = ParameterIn.HEADER,
                    required = true,
                    example =
                            "entrega-550e8400-e29b-41d4-a716-446655440000",
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
            @PathVariable Long idPedido) {
        return idempotencyService.executar(
                key,
                request,
                null,
                PedidoResponseDTO.class,
                () -> pedidoService.entregarPedido(idPedido),
                "Pedido entregue com sucesso"
        );
    }

    @Operation(
            summary = "Cancelar pedido",
            description = """
                    Cancela o pedido do usuário autenticado ou solicita
                    cancelamento, conforme o status atual do pedido.

                    A operação exige o header Idempotency-Key.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Cancelamento realizado ou resposta idempotente
                            recuperada
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPedidoOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pedidoCancelado",
                                            summary = "Pedido cancelado",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "CANCELADO",
                                                        "valorBruto": 300.00,
                                                        "valorDesconto": 0.00,
                                                        "valorFinal": 300.00,
                                                        "codigoCupom": null,
                                                        "itens": [
                                                          {
                                                            "itemPedidoId": 21,
                                                            "produtoId": 5,
                                                            "nomeProduto": "Teclado Mecânico",
                                                            "quantidade": 2,
                                                            "precoUnitario": 150.00,
                                                            "subtotal": 300.00
                                                          }
                                                        ]
                                                      },
                                                      "mensagem": "Cancelamento do pedido realizado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "cancelamentoJaProcessado",
                                            summary = "Resposta idempotente recuperada",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "CANCELADO",
                                                        "valorBruto": 300.00,
                                                        "valorDesconto": 0.00,
                                                        "valorFinal": 300.00,
                                                        "codigoCupom": null,
                                                        "itens": [
                                                          {
                                                            "itemPedidoId": 21,
                                                            "produtoId": 5,
                                                            "nomeProduto": "Teclado Mecânico",
                                                            "quantidade": 2,
                                                            "precoUnitario": 150.00,
                                                            "subtotal": 300.00
                                                          }
                                                        ]
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
                    description = "Idempotency-Key ausente ou inválida",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "headerIdempotenciaAusente",
                                    summary = "Header obrigatório não enviado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Header obrigatório não enviado: Idempotency-Key"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoEncontrado",
                                    summary = "Pedido inexistente",
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
                    description = "Estado atual não permite cancelar o pedido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoPodeSerCancelado",
                                    summary = "Estado incompatível",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Pedido com status ENVIADO não pode ser cancelado."
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{idPedido}/cancel")
    public RespostaApi<PedidoResponseDTO> cancelarPedido(
            @Parameter(
                    name = "Idempotency-Key",
                    description = """
                            Chave única da operação.

                            Pode possuir no máximo 255 caracteres e
                            permanece válida por 24 horas.
                            """,
                    in = ParameterIn.HEADER,
                    required = true,
                    example =
                            "cancelamento-550e8400-e29b-41d4-a716-446655440000",
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
            @PathVariable Long idPedido) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return idempotencyService.executar(
                key,
                request,
                null,
                PedidoResponseDTO.class,
                () -> pedidoService.cancelarPedido(idPedido, usuario),
                "Cancelamento do pedido realizado com sucesso"
        );
    }

    @Operation(
            summary = "Estornar pedido",
            description = """
                    Estorna um pedido que esteja com status
                    CANCELAMENTO_SOLICITADO.

                    Esta operação é exclusiva para administradores e exige
                    o header Idempotency-Key.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Pedido estornado ou resposta idempotente recuperada
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPedidoOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "pedidoEstornado",
                                            summary = "Pedido estornado",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "ESTORNADO",
                                                        "valorBruto": 300.00,
                                                        "valorDesconto": 0.00,
                                                        "valorFinal": 300.00,
                                                        "codigoCupom": null,
                                                        "itens": [
                                                          {
                                                            "itemPedidoId": 21,
                                                            "produtoId": 5,
                                                            "nomeProduto": "Teclado Mecânico",
                                                            "quantidade": 2,
                                                            "precoUnitario": 150.00,
                                                            "subtotal": 300.00
                                                          }
                                                        ]
                                                      },
                                                      "mensagem": "Pedido estornado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "estornoJaProcessado",
                                            summary = "Resposta idempotente recuperada",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "idPedido": 10,
                                                        "idUsuario": 2,
                                                        "dataCriacao": "2026-07-18T14:30:00",
                                                        "status": "ESTORNADO",
                                                        "valorBruto": 300.00,
                                                        "valorDesconto": 0.00,
                                                        "valorFinal": 300.00,
                                                        "codigoCupom": null,
                                                        "itens": [
                                                          {
                                                            "itemPedidoId": 21,
                                                            "produtoId": 5,
                                                            "nomeProduto": "Teclado Mecânico",
                                                            "quantidade": 2,
                                                            "precoUnitario": 150.00,
                                                            "subtotal": 300.00
                                                          }
                                                        ]
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
                    description = "Idempotency-Key ausente ou inválida",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "headerIdempotenciaAusente",
                                    summary = "Header obrigatório não enviado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Header obrigatório não enviado: Idempotency-Key"
                                            }
                                            """
                            )
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
                    responseCode = "404",
                    description = "Pedido não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoEncontrado",
                                    summary = "Pedido inexistente",
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
                    description = "Estado atual não permite estornar o pedido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoPodeSerEstornado",
                                    summary = "Estado incompatível",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Pedido com status PAGO não pode ser estornado."
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{idPedido}/refund")
    public RespostaApi<PedidoResponseDTO> estornarPedido(
            @Parameter(
                    name = "Idempotency-Key",
                    description = """
                            Chave única da operação.

                            Pode possuir no máximo 255 caracteres e
                            permanece válida por 24 horas.
                            """,
                    in = ParameterIn.HEADER,
                    required = true,
                    example =
                            "estorno-550e8400-e29b-41d4-a716-446655440000",
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
            @PathVariable Long idPedido) {
        return idempotencyService.executar(
                key,
                request,
                null,
                PedidoResponseDTO.class,
                () -> pedidoService.estornarPedido(idPedido),
                "Pedido estornado com sucesso"
        );
    }

}
