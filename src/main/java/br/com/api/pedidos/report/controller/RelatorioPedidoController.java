package br.com.api.pedidos.report.controller;

import br.com.api.pedidos.report.dto.PeriodoRelatorioFiltroDTO;
import br.com.api.pedidos.report.dto.ResumoPedidosResponseDTO;
import br.com.api.pedidos.report.dto.openapi.RespostaResumoPedidosOpenApiDTO;
import br.com.api.pedidos.report.service.RelatorioPedidoService;
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
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Relatórios de pedidos",
        description = """
                Indicadores administrativos consolidados dos pedidos.

                O relatório pode considerar todos os pedidos ou um período
                específico definido pelas datas inicial e final.

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
@RequestMapping("/admin/reports/orders")
@PreAuthorize("hasRole('ADMIN')")
public class RelatorioPedidoController {

    private final RelatorioPedidoService relatorioPedidoService;

    public RelatorioPedidoController(
            RelatorioPedidoService relatorioPedidoService
    ) {
        this.relatorioPedidoService = relatorioPedidoService;
    }

    @Operation(
            summary = "Gerar resumo dos pedidos",
            description = """
                    Retorna indicadores consolidados dos pedidos cadastrados.

                    Os parâmetros dataInicio e dataFim são opcionais.

                    Quando nenhuma data é informada, todos os pedidos são
                    considerados.

                    Quando somente dataInicio é informada, são considerados
                    os pedidos criados a partir do início dessa data.

                    Quando somente dataFim é informada, são considerados os
                    pedidos criados até o final dessa data.

                    Quando as duas datas são informadas, ambas são inclusivas.

                    São considerados como venda os pedidos com status:

                    PAGO, ENVIADO e ENTREGUE.

                    O valor total vendido corresponde à soma do valor final
                    desses pedidos.

                    O ticket médio corresponde ao valor total vendido dividido
                    pela quantidade de pedidos contabilizados como venda.
                    """,
            parameters = {
                    @Parameter(
                            name = "dataInicio",
                            description = """
                                    Data inicial inclusiva do relatório.

                                    Formato esperado: yyyy-MM-dd.
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
                                    Data final inclusiva do relatório.

                                    Formato esperado: yyyy-MM-dd.
                                    """,
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "2026-07-31",
                            schema = @Schema(
                                    type = "string",
                                    format = "date"
                            )
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumo dos pedidos gerado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaResumoPedidosOpenApiDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "resumoComPedidos",
                                            summary = """
                                                    Indicadores encontrados
                                                    para o período
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "totalPedidos": 12,
                                                        "totalPedidosPagos": 8,
                                                        "totalPedidosCancelados": 2,
                                                        "totalPedidosAguardandoPagamento": 1,
                                                        "valorTotalVendido": 2400.00,
                                                        "ticketMedio": 300.00
                                                      },
                                                      "mensagem": "Resumo dos pedidos gerado com sucesso"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "resumoSemPedidos",
                                            summary = """
                                                    Nenhum pedido encontrado
                                                    para o período
                                                    """,
                                            value = """
                                                    {
                                                      "sucesso": true,
                                                      "dados": {
                                                        "totalPedidos": 0,
                                                        "totalPedidosPagos": 0,
                                                        "totalPedidosCancelados": 0,
                                                        "totalPedidosAguardandoPagamento": 0,
                                                        "valorTotalVendido": 0.00,
                                                        "ticketMedio": 0.00
                                                      },
                                                      "mensagem": "Resumo dos pedidos gerado com sucesso"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Período informado é inválido
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
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
                            )
                    )
            )
    })
    @GetMapping("/summary")
    public RespostaApi<ResumoPedidosResponseDTO> gerarResumo(
            @Parameter(hidden = true)
            @Valid
            @ModelAttribute
            PeriodoRelatorioFiltroDTO filtro
    ) {
        return RespostaApi.sucesso(
                relatorioPedidoService.gerarResumo(filtro),
                "Resumo dos pedidos gerado com sucesso"
        );
    }

}
