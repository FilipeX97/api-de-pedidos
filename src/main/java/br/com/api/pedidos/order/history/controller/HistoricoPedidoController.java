package br.com.api.pedidos.order.history.controller;

import br.com.api.pedidos.order.history.dto.HistoricoPedidoResponseDTO;
import br.com.api.pedidos.order.history.dto.openapi.RespostaListaHistoricoPedidoOpenApiDTO;
import br.com.api.pedidos.order.history.service.HistoricoPedidoService;
import br.com.api.pedidos.order.service.PedidoService;
import br.com.api.pedidos.security.service.UsuarioLogadoService;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroOpenApiDTO;
import br.com.api.pedidos.shared.response.RespostaApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "Histórico de pedidos",
        description = """
                Consulta dos eventos registrados durante o ciclo de vida
                de um pedido.

                O usuário autenticado pode consultar somente o histórico
                dos próprios pedidos.

                Os registros são retornados do evento mais recente para
                o mais antigo.
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
@RequestMapping("/orders/{idPedido}/history")
public class HistoricoPedidoController {

    private final HistoricoPedidoService historicoPedidoService;
    private final PedidoService pedidoService;
    private final UsuarioLogadoService usuarioLogadoService;

    public HistoricoPedidoController(
            HistoricoPedidoService historicoPedidoService,
            PedidoService pedidoService,
            UsuarioLogadoService usuarioLogadoService) {
        this.historicoPedidoService = historicoPedidoService;
        this.pedidoService = pedidoService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    @Operation(
            summary = "Listar histórico do pedido",
            description = """
                    Retorna os eventos registrados durante o ciclo de vida
                    de um pedido pertencente ao usuário autenticado.

                    Antes de consultar o histórico, a aplicação verifica
                    se o pedido pertence ao usuário autenticado.

                    Um pedido pertencente a outro usuário é tratado como
                    não encontrado.

                    Os registros são ordenados pela data de criação em ordem
                    decrescente, retornando primeiro o evento mais recente.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Histórico do pedido encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaListaHistoricoPedidoOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "historicoEncontrado",
                                            summary = """
                                                    Pedido possui registros
                                                    de histórico
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": [
                                                        {
                                                          "id": 25,
                                                          "idPedido": 10,
                                                          "status": "ENTREGUE",
                                                          "descricao": "Pedido entregue",
                                                          "dataCriacao": "2026-07-19T17:30:00"
                                                        },
                                                        {
                                                          "id": 24,
                                                          "idPedido": 10,
                                                          "status": "ENVIADO",
                                                          "descricao": "Pedido enviado",
                                                          "dataCriacao": "2026-07-19T16:30:00"
                                                        },
                                                        {
                                                          "id": 23,
                                                          "idPedido": 10,
                                                          "status": "PAGO",
                                                          "descricao": "Pedido pago com sucesso",
                                                          "dataCriacao": "2026-07-19T15:45:00"
                                                        },
                                                        {
                                                          "id": 22,
                                                          "idPedido": 10,
                                                          "status": "CRIADO",
                                                          "descricao": "Cupom aplicado: DESCONTO15",
                                                          "dataCriacao": "2026-07-19T15:20:00"
                                                        },
                                                        {
                                                          "id": 21,
                                                          "idPedido": 10,
                                                          "status": "CRIADO",
                                                          "descricao": "Pedido Criado",
                                                          "dataCriacao": "2026-07-19T15:00:00"
                                                        }
                                                      ],
                                                      "mensagem": "Histórico do pedido encontrado"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "historicoVazio",
                                            summary = """
                                                    Pedido ainda não possui
                                                    registros de histórico
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": [],
                                                      "mensagem": "Histórico do pedido encontrado"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Identificador do pedido informado em formato inválido
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
                            Pedido não encontrado ou pertencente a outro usuário
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "pedidoNaoEncontrado",
                                    summary = """
                                            Pedido inexistente ou sem acesso
                                            para o usuário autenticado
                                            """,
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
    public RespostaApi<List<HistoricoPedidoResponseDTO>> listarHistorico(
            @Parameter(
                    description = "Identificador do pedido",
                    required = true,
                    example = "10"
            )
            @PathVariable Long idPedido) {
        var usuario = usuarioLogadoService.getUsuarioLogado();
        pedidoService.buscarPedidoPorId(idPedido, usuario);

        return RespostaApi.sucesso(
                historicoPedidoService.listarPorPedido(idPedido),
                "Histórico do pedido encontrado"
        );
    }
}
