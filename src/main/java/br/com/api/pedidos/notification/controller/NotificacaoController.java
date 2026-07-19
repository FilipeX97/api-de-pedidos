package br.com.api.pedidos.notification.controller;

import br.com.api.pedidos.notification.dto.NotificacaoResponseDTO;
import br.com.api.pedidos.notification.dto.openapi.RespostaNotificacaoOpenApiDTO;
import br.com.api.pedidos.notification.dto.openapi.RespostaPaginaNotificacoesOpenApiDTO;
import br.com.api.pedidos.notification.dto.openapi.RespostaQuantidadeNotificacoesNaoLidasOpenApiDTO;
import br.com.api.pedidos.notification.service.NotificacaoService;
import br.com.api.pedidos.security.service.UsuarioLogadoService;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroOpenApiDTO;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Notificações",
        description = """
                Consulta e gerenciamento das notificações do usuário
                autenticado.

                Cada usuário visualiza somente as próprias notificações.

                As notificações são criadas automaticamente a partir dos
                eventos relacionados aos pedidos.
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
@RequestMapping("/notifications")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final UsuarioLogadoService usuarioLogadoService;

    public NotificacaoController(
            NotificacaoService notificacaoService,
            UsuarioLogadoService usuarioLogadoService) {
        this.notificacaoService = notificacaoService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    @Operation(
            summary = "Listar minhas notificações",
            description = """
                    Retorna de forma paginada somente as notificações
                    pertencentes ao usuário autenticado.

                    O parâmetro lida é opcional:

                    - lida=true retorna somente notificações lidas;
                    - lida=false retorna somente notificações não lidas;
                    - quando omitido, retorna todas as notificações.

                    Campos permitidos para ordenação:

                    id, titulo, tipo, lida e dataCriacao.

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
                                    Quantidade de notificações por página.

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
                                    id, titulo, tipo, lida e dataCriacao.

                                    É possível enviar mais de um parâmetro
                                    sort.

                                    Exemplos:
                                    sort=dataCriacao,desc
                                    sort=lida,asc&sort=dataCriacao,desc
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
                    description = "Notificações encontradas",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPaginaNotificacoesOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "notificacoesEncontradas",
                                    summary = """
                                            Página de notificações encontrada
                                            """,
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "conteudo": [
                                                  {
                                                    "id": 100,
                                                    "idUsuario": 1,
                                                    "idPedido": 10,
                                                    "titulo": "Pagamento confirmado",
                                                    "mensagem": "O pagamento do pedido #10 foi confirmado",
                                                    "tipo": "PEDIDO_PAGO",
                                                    "lida": false,
                                                    "dataCriacao": "2026-07-19T15:30:00"
                                                  },
                                                  {
                                                    "id": 99,
                                                    "idUsuario": 1,
                                                    "idPedido": 10,
                                                    "titulo": "Pedido criado",
                                                    "mensagem": "Seu pedido #10 foi criado com sucesso.",
                                                    "tipo": "PEDIDO_CRIADO",
                                                    "lida": true,
                                                    "dataCriacao": "2026-07-19T15:00:00"
                                                  }
                                                ],
                                                "paginaAtual": 0,
                                                "totalPaginas": 1,
                                                "totalElementos": 2,
                                                "tamanhoPagina": 20,
                                                "quantidadeElementos": 2,
                                                "primeiraPagina": true,
                                                "ultimaPagina": true,
                                                "vazia": false
                                              },
                                              "mensagem": "Notificações encontradas"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Filtro ou parâmetros de paginação inválidos
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "filtroLidaInvalido",
                                            summary = """
                                                    Valor inválido para
                                                    o filtro lida
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Parâmetro inválido: lida"
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
                                    )
                            }
                    )
            )
    })
    @GetMapping
    public RespostaApi<PaginaResponseDTO<NotificacaoResponseDTO>> listarMinhasNotificacoes(
            @Parameter(
                    description = """
                            Filtra as notificações pelo estado de leitura.

                            true retorna notificações lidas.
                            false retorna notificações não lidas.
                            Quando omitido, retorna todas.
                            """,
                    required = false,
                    example = "false",
                    schema = @Schema(
                            type = "boolean",
                            allowableValues = {
                                    "true",
                                    "false"
                            }
                    )
            )
            @RequestParam(required = false)
            Boolean lida,

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
                notificacaoService.listarPorUsuario(
                        usuario,
                        lida,
                        pageable
                ),
                "Notificações encontradas"
        );
    }

    @Operation(
            summary = "Contar notificações não lidas",
            description = """
                    Retorna a quantidade de notificações não lidas
                    pertencentes ao usuário autenticado.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Quantidade de notificações não lidas encontrada
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaQuantidadeNotificacoesNaoLidasOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "quantidadeNaoLidas",
                                    summary = """
                                            Usuário possui notificações
                                            não lidas
                                            """,
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": 3,
                                              "mensagem": "Quantidade de notificações não lidas encontrada"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/unread-count")
    public RespostaApi<Long> contarNaoLidas() {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                notificacaoService.contarNaoLidas(usuario),
                "Quantidade de notificações não lidas encontrada"
        );
    }

    @Operation(
            summary = "Marcar notificação como lida",
            description = """
                    Marca como lida uma notificação pertencente ao usuário
                    autenticado.

                    Uma notificação pertencente a outro usuário não pode
                    ser acessada por este endpoint.

                    Caso a notificação já esteja lida, a operação continua
                    retornando sucesso.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notificação marcada como lida",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaNotificacaoOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "notificacaoMarcadaComoLida",
                                    summary = """
                                            Notificação atualizada com sucesso
                                            """,
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "id": 100,
                                                "idUsuario": 1,
                                                "idPedido": 10,
                                                "titulo": "Pagamento confirmado",
                                                "mensagem": "O pagamento do pedido #10 foi confirmado",
                                                "tipo": "PEDIDO_PAGO",
                                                "lida": true,
                                                "dataCriacao": "2026-07-19T15:30:00"
                                              },
                                              "mensagem": "Notificação marcada como lida"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Identificador inválido ou notificação não encontrada
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "idNotificacaoInvalido",
                                            summary = """
                                                    Identificador incompatível
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Parâmetro inválido: idNotificacao"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "notificacaoNaoEncontrada",
                                            summary = """
                                                    Notificação inexistente ou
                                                    pertencente a outro usuário
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "Notificação não encontrada"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PatchMapping("/{idNotificacao}/read")
    public RespostaApi<NotificacaoResponseDTO> marcarComoLida(
            @Parameter(
                    description = "Identificador da notificação",
                    required = true,
                    example = "100"
            )
            @PathVariable Long idNotificacao) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                notificacaoService.marcarComoLida(idNotificacao, usuario),
                "Notificação marcada como lida"
        );
    }
}
