package br.com.api.pedidos.product.controller;

import br.com.api.pedidos.product.dto.ProdutoAtualizacaoRequest;
import br.com.api.pedidos.product.dto.ProdutoCriacaoRequestDTO;
import br.com.api.pedidos.product.dto.ProdutoResponseDTO;
import br.com.api.pedidos.product.dto.openapi.RespostaPaginaProdutosOpenApiDTO;
import br.com.api.pedidos.product.dto.openapi.RespostaProdutoOpenApiDTO;
import br.com.api.pedidos.product.service.ProdutoService;
import br.com.api.pedidos.shared.idempotency.service.IdempotencyService;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroOpenApiDTO;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroValidacaoOpenApiDTO;
import br.com.api.pedidos.shared.openapi.dto.RespostaSucessoSemDadosOpenApiDTO;
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
        name = "Produtos",
        description = """
                Consulta e gerenciamento do catálogo de produtos.

                Usuários autenticados podem consultar produtos.
                As operações de cadastro, atualização e remoção são
                exclusivas para administradores.
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
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final IdempotencyService idempotencyService;

    public ProdutoController(
            ProdutoService produtoService,
            IdempotencyService idempotencyService
    ) {
        this.produtoService = produtoService;
        this.idempotencyService = idempotencyService;
    }

    @Operation(
            summary = "Buscar produto por ID",
            description = """
                    Retorna os dados de um produto pelo identificador.

                    Esta operação exige autenticação, mas não exige
                    perfil administrativo.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaProdutoOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "produtoEncontrado",
                                    summary = "Produto encontrado com sucesso",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "id": 1,
                                                "nome": "Notebook Dell Inspiron 15",
                                                "descricao": "Notebook com 16 GB de memória RAM",
                                                "preco": 3499.90,
                                                "estoque": 25,
                                                "ativo": true
                                              },
                                              "mensagem": "Produto encontrado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            Token não enviado, inválido, expirado ou bloqueado
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
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
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
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
                                    implementation = RespostaErroOpenApiDTO.class
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
    @GetMapping("/{id}")
    public RespostaApi<ProdutoResponseDTO> buscarProdutoPorId(
            @Parameter(
                    description = "Identificador do produto",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        return RespostaApi.sucesso(
                produtoService.buscarProdutoPorId(id),
                "Produto encontrado"
        );
    }

    @Operation(
            summary = "Listar produtos",
            description = """
                    Retorna os produtos de forma paginada.

                    Campos permitidos para ordenação:

                    id, nome, descricao, preco, estoque e ativo.

                    O tamanho máximo permitido para uma página é 100.
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
                                    Quantidade de produtos por página.

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
                                    id, nome, descricao, preco, estoque e ativo.

                                    Exemplos:
                                    sort=nome,asc
                                    sort=preco,desc
                                    sort=ativo,desc&sort=nome,asc
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "nome,asc",
                            array = @ArraySchema(
                                    arraySchema = @Schema(
                                            defaultValue = "nome,asc"
                                    ),
                                    schema = @Schema(
                                            type = "string",
                                            example = "nome,asc"
                                    )
                            )
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de produtos encontrada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPaginaProdutosOpenApiDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Paginação ou campo de ordenação inválido
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "tamanhoPaginaInvalido",
                                            summary = """
                                                    Tamanho máximo da página excedido
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
                                                    Campo de ordenação não permitido
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Campo de ordenação inválido: dataCriacao. Campos permitidos: [id, nome, descricao, preco, estoque, ativo]"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            Token não enviado, inválido, expirado ou bloqueado
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
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
                                    )
                            }
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
                                    implementation = RespostaErroOpenApiDTO.class
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
    @GetMapping
    public RespostaApi<PaginaResponseDTO<ProdutoResponseDTO>> listarProdutos(
            @Parameter(hidden = true)
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "nome",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return RespostaApi.sucesso(
                produtoService.listarProdutos(pageable),
                "Produtos encontrados"
        );
    }

    @Operation(
            summary = "Cadastrar produto",
            description = """
                    Cadastra um novo produto ativo no catálogo.

                    A operação é exclusiva para administradores e exige
                    o header Idempotency-Key.

                    Repetir a mesma chave com o mesmo conteúdo retorna o
                    resultado já processado. Reutilizar a chave com um
                    conteúdo diferente retorna conflito.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = """
                            Produto criado ou resposta idempotente recuperada
                            com sucesso
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaProdutoOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "produtoCriado",
                                            summary = "Produto criado",
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "id": 1,
                                                        "nome": "Notebook Dell Inspiron 15",
                                                        "descricao": "Notebook com 16 GB de memória RAM",
                                                        "preco": 3499.90,
                                                        "estoque": 25,
                                                        "ativo": true
                                                      },
                                                      "mensagem": "Produto criado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "respostaIdempotente",
                                            summary = """
                                                    Operação processada anteriormente
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "id": 1,
                                                        "nome": "Notebook Dell Inspiron 15",
                                                        "descricao": "Notebook com 16 GB de memória RAM",
                                                        "preco": 3499.90,
                                                        "estoque": 25,
                                                        "ativo": true
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
                            Dados inválidos, JSON inválido ou Idempotency-Key
                            ausente, vazia ou maior que 255 caracteres
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
                                            name = "dadosProdutoInvalidos",
                                            summary = """
                                                    Campos do produto inválidos
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": {
                                                        "nome": "Nome é obrigatório",
                                                        "preco": "Preço deve ser maior que zero",
                                                        "estoque": "Estoque não pode ser negativo"
                                                      },
                                                      "mensagem": "Dados inválidos"
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
                                                    Header Idempotency-Key não enviado
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
                                            summary = """
                                                    Idempotency-Key vazia
                                                    """,
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
                                                    Idempotency-Key maior que 255 caracteres
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
                    responseCode = "401",
                    description = """
                            Token não enviado, inválido, expirado ou bloqueado
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
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
                                    implementation = RespostaErroOpenApiDTO.class
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
                    responseCode = "409",
                    description = """
                            Idempotency-Key reutilizada com um conteúdo diferente
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "chaveReutilizada",
                                    summary = """
                                            Chave reutilizada com payload diferente
                                            """,
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Idempotency-Key reutilizada com payload diferente"
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
                                    implementation = RespostaErroOpenApiDTO.class
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
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaApi<ProdutoResponseDTO> criarProduto(
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
                            "produto-550e8400-e29b-41d4-a716-446655440000",
                    schema = @Schema(
                            type = "string",
                            maxLength = 255
                    )
            )
            @RequestHeader("Idempotency-Key") String key,

            @Valid
            @RequestBody
            ProdutoCriacaoRequestDTO dto,

            HttpServletRequest request
    ) {
        return idempotencyService.executar(
                key,
                request,
                dto,
                ProdutoResponseDTO.class,
                () -> produtoService.criarProduto(dto),
                "Produto criado com sucesso"
        );
    }

    @Operation(
            summary = "Atualizar produto parcialmente",
            description = """
                    Atualiza somente os campos enviados no corpo da requisição.

                    Esta operação é exclusiva para administradores.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto atualizado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaProdutoOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "produtoAtualizado",
                                    summary = "Produto atualizado",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "id": 1,
                                                "nome": "Notebook Dell Inspiron 15 Plus",
                                                "descricao": "Notebook com processador atualizado",
                                                "preco": 3299.90,
                                                "estoque": 30,
                                                "ativo": true
                                              },
                                              "mensagem": "Produto atualizado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Dados de atualização ou JSON inválido
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
                                            name = "dadosAtualizacaoInvalidos",
                                            summary = """
                                                    Campos de atualização inválidos
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": {
                                                        "nome": "Nome não pode estar em branco",
                                                        "preco": "Preço deve ser maior que zero",
                                                        "estoque": "Estoque não pode ser negativo"
                                                      },
                                                      "mensagem": "Dados inválidos"
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
                    responseCode = "401",
                    description = """
                            Token não enviado, inválido, expirado ou bloqueado
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
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
                                    implementation = RespostaErroOpenApiDTO.class
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
                    responseCode = "404",
                    description = "Produto não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
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
                                    implementation = RespostaErroOpenApiDTO.class
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
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public RespostaApi<ProdutoResponseDTO> atualizarProduto(
            @Parameter(
                    description = "Identificador do produto",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            ProdutoAtualizacaoRequest produtoAtualizacaoRequest
    ) {
        return RespostaApi.sucesso(
                produtoService.atualizarProduto(
                        id,
                        produtoAtualizacaoRequest
                ),
                "Produto atualizado"
        );
    }

    @Operation(
            summary = "Remover produto",
            description = """
                    Remove permanentemente um produto do catálogo.

                    A operação é exclusiva para administradores.

                    Produtos associados a registros existentes podem não ser
                    removidos devido às regras de integridade do banco.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto removido com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaSucessoSemDadosOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "produtoRemovido",
                                    summary = "Produto removido",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": null,
                                              "mensagem": "Produto removido com sucesso"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            Token não enviado, inválido, expirado ou bloqueado
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
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
                                    implementation = RespostaErroOpenApiDTO.class
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
                    responseCode = "404",
                    description = "Produto não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
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
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                            Produto possui registros relacionados e não pode
                            ser removido
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "produtoRelacionado",
                                    summary = """
                                            Produto possui registros relacionados
                                            """,
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Violação de integridade dos dados"
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
                                    implementation = RespostaErroOpenApiDTO.class
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
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public RespostaApi<Void> removerProduto(
            @Parameter(
                    description = "Identificador do produto",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        produtoService.removerProduto(id);

        return RespostaApi.sucesso(
                null,
                "Produto removido com sucesso"
        );
    }
}