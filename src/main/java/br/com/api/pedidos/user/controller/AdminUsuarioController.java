package br.com.api.pedidos.user.controller;

import br.com.api.pedidos.shared.openapi.dto.RespostaErroOpenApiDTO;
import br.com.api.pedidos.shared.openapi.dto.RespostaSucessoSemDadosOpenApiDTO;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.response.RespostaApi;
import br.com.api.pedidos.user.dto.UsuarioResponseDTO;
import br.com.api.pedidos.user.dto.openapi.RespostaPaginaUsuariosOpenApiDTO;
import br.com.api.pedidos.user.service.UsuarioService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Administração de usuários",
        description = """
                Operações administrativas de listagem, ativação,
                desativação e remoção de usuários
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
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(
            summary = "Listar usuários",
            description = """
                Retorna os usuários de forma paginada.

                Campos permitidos para ordenação:

                id, nome, email, perfil, dataCriacao, ativo e clienteVip.

                O tamanho máximo permitido para uma página é 100.
                """,
            parameters = {
                    @Parameter(
                            name = "page",
                            description = """
                                Número da página que será consultada.

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
                                Quantidade de usuários retornados por página.

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
                                Critério de ordenação no formato:

                                campo,direção

                                Direções permitidas: asc e desc.

                                Campos permitidos:
                                id, nome, email, perfil, dataCriacao,
                                ativo e clienteVip.

                                O parâmetro pode ser repetido para aplicar
                                múltiplos critérios de ordenação.

                                Exemplos:
                                sort=nome,asc
                                sort=perfil,asc&sort=nome,desc
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
                    description = "Página de usuários encontrada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaPaginaUsuariosOpenApiDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Paginação ou campo de ordenação inválido",
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
                                            name = "campoOrdenacaoInvalido",
                                            summary = """
                                                Campo de ordenação não permitido
                                                """,
                                            value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Campo de ordenação inválido: preco. Campos permitidos: [id, nome, email, perfil, dataCriacao, ativo, clienteVip]"
                                                }
                                                """
                                    )
                            }
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public RespostaApi<PaginaResponseDTO<UsuarioResponseDTO>> listarUsuarios(
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
                usuarioService.listarUsuarios(pageable),
                "Usuários encontrados"
        );
    }

    @Operation(
            summary = "Ativar usuário",
            description = """
                    Ativa um usuário anteriormente desativado,
                    permitindo que ele volte a autenticar.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário ativado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaSucessoSemDadosOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioAtivado",
                                    summary = "Usuário ativado",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": null,
                                              "mensagem": "Usuário ativado com sucesso"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioNaoEncontrado",
                                    summary = "Não existe usuário com o ID informado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Usuário não encontrado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Usuário já está ativo",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioJaAtivo",
                                    summary = "Usuário já se encontra ativo",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Usuário já está ativo"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/ativar")
    public RespostaApi<Void> ativar(@PathVariable Long id) {
        usuarioService.ativarUsuario(id);
        return RespostaApi.sucesso(null, "Usuario ativado com sucesso");
    }

    @Operation(
            summary = "Desativar usuário",
            description = """
                    Desativa o usuário e invalida os tokens emitidos
                    anteriormente.

                    Um usuário desativado não pode realizar novos logins.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário desativado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaSucessoSemDadosOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioDesativado",
                                    summary = "Usuário desativado",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": null,
                                              "mensagem": "Usuário desativado com sucesso"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioNaoEncontrado",
                                    summary = "Não existe usuário com o ID informado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Usuário não encontrado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Usuário já está desativado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioJaDesativado",
                                    summary = "Usuário já se encontra desativado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Usuário já está desativado"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public RespostaApi<Void> desativar(@PathVariable Long id) {
        usuarioService.desativarUsuario(id);
        return RespostaApi.sucesso(null, "Usuario desativado com sucesso");
    }

    @Operation(
            summary = "Remover usuário",
            description = """
                    Remove permanentemente o usuário.

                    Antes da remoção, os tokens emitidos para o usuário
                    são invalidados.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário removido com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaSucessoSemDadosOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioRemovido",
                                    summary = "Usuário removido",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": null,
                                              "mensagem": "Usuário removido com sucesso"
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
                    description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioNaoEncontrado",
                                    summary = "Não existe usuário com o ID informado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Usuário não encontrado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                            Usuário não pode ser removido porque possui
                            registros relacionados
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "violacaoIntegridade",
                                    summary = "Usuário possui registros relacionados",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Violação de integridade dos dados"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public RespostaApi<Void> removerUsuario(@PathVariable Long id) {
        usuarioService.removerUsuario(id);
        return RespostaApi.sucesso(null, "Usuário removido com sucesso");
    }

}
