package br.com.api.pedidos.user.controller;

import br.com.api.pedidos.shared.openapi.dto.RespostaErroOpenApiDTO;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroValidacaoOpenApiDTO;
import br.com.api.pedidos.shared.response.RespostaApi;
import br.com.api.pedidos.user.dto.UsuarioAtualizacaoRequest;
import br.com.api.pedidos.user.dto.UsuarioCriacaoRequestDTO;
import br.com.api.pedidos.user.dto.UsuarioResponseDTO;
import br.com.api.pedidos.user.dto.openapi.RespostaUsuarioOpenApiDTO;
import br.com.api.pedidos.user.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Usuários",
        description = """
                Consulta e atualização de usuários.

                Um usuário comum pode consultar e alterar somente os próprios
                dados. Administradores podem acessar qualquer usuário.
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
                        Usuário autenticado não possui permissão para a operação
                        """,
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(
                                implementation =
                                        RespostaErroOpenApiDTO.class
                        ),
                        examples = @ExampleObject(
                                name = "acessoNegado",
                                summary = "Acesso negado",
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
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(
            summary = "Cadastrar usuário administrativamente",
            description = """
                    Cadastra um novo usuário com perfil USER.

                    Esta operação é diferente de POST /auth/registrar porque
                    somente administradores podem utilizá-la.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário cadastrado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaUsuarioOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioCadastrado",
                                    summary = "Usuário cadastrado com sucesso",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "id": 1,
                                                "nome": "Maria da Silva",
                                                "email": "maria.silva@exemplo.com",
                                                "perfil": "USER",
                                                "dataCriacao": "2026-07-17"
                                              },
                                              "mensagem": "Usuário cadastrado com sucesso"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Dados inválidos, JSON inválido ou e-mail já cadastrado
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
                                            name = "dadosInvalidos",
                                            summary = "Campos enviados são inválidos",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": {
                                                        "email": "E-mail inválido",
                                                        "senha": "Senha deve ter entre 6 e 100 caracteres"
                                                      },
                                                      "mensagem": "Dados inválidos"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "emailJaCadastrado",
                                            summary = "E-mail já utilizado",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "E-mail já cadastrado"
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
                    responseCode = "409",
                    description = "Conflito de integridade no banco de dados",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "violacaoIntegridade",
                                    summary = "Violação de integridade dos dados",
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
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RespostaApi<UsuarioResponseDTO> cadastrarUsuario(
            @Valid @RequestBody UsuarioCriacaoRequestDTO usuarioCriacaoRequestDTO) {
        return RespostaApi.sucesso(usuarioService.cadastrarUsuario(usuarioCriacaoRequestDTO),
                "Usuário cadastrado com sucesso");
    }

    @Operation(
            summary = "Buscar usuário por ID",
            description = """
                    Retorna um usuário pelo identificador.

                    Um usuário comum pode consultar somente o próprio cadastro.
                    Administradores podem consultar qualquer usuário.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaUsuarioOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioEncontrado",
                                    summary = "Usuário encontrado",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "id": 1,
                                                "nome": "Maria da Silva",
                                                "email": "maria.silva@exemplo.com",
                                                "perfil": "USER",
                                                "dataCriacao": "2026-07-17"
                                              },
                                              "mensagem": "Usuário encontrado"
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
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.usuario.id or hasRole('ADMIN')")
    public RespostaApi<UsuarioResponseDTO> buscarUsuarioPorId(
            @Parameter(
                    description = "Identificador do usuário",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ){
        return RespostaApi.sucesso(usuarioService.buscarUsuarioPorId(id),
                "Usuário encontrado");
    }

    @Operation(
            summary = "Buscar usuário por e-mail",
            description = """
                    Consulta um usuário pelo e-mail cadastrado.

                    Esta operação é exclusiva para administradores.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaUsuarioOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioEncontrado",
                                    summary = "Usuário encontrado",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "id": 1,
                                                "nome": "Maria da Silva",
                                                "email": "maria.silva@exemplo.com",
                                                "perfil": "USER",
                                                "dataCriacao": "2026-07-17"
                                              },
                                              "mensagem": "Usuário encontrado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetro email não informado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "emailNaoInformado",
                                    summary = "Parâmetro obrigatório ausente",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Parâmetro obrigatório não enviado: email"
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
                                    summary = "Não existe usuário com o e-mail informado",
                                    value = """
                                            {
                                              "sucesso": false,
                                              "dados": null,
                                              "mensagem": "Usuário não encontrado"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/email")
    @PreAuthorize("hasRole('ADMIN')")
    public RespostaApi<UsuarioResponseDTO> buscarUsuarioPorEmail(
            @Parameter(
                    description = "E-mail exato do usuário",
                    required = true,
                    example = "maria.silva@exemplo.com"
            )
            @RequestParam String email
    ) {
        return RespostaApi.sucesso(usuarioService.buscarUsuarioPorEmail(email),
                "Usuário encontrado");
    }

    @Operation(
            summary = "Atualizar usuário parcialmente",
            description = """
                    Atualiza somente os campos enviados no corpo da requisição.

                    Um usuário comum pode alterar somente o próprio cadastro.
                    Administradores podem alterar qualquer usuário.

                    Caso o e-mail ou a senha sejam alterados, tokens emitidos
                    anteriormente deixam de ser aceitos.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário atualizado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaUsuarioOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioAtualizado",
                                    summary = "Usuário atualizado",
                                    value = """
                                            {
                                              "sucesso": true,
                                              "dados": {
                                                "id": 1,
                                                "nome": "Maria Souza",
                                                "email": "maria.souza@exemplo.com",
                                                "perfil": "USER",
                                                "dataCriacao": "2026-07-17"
                                              },
                                              "mensagem": "Usuário atualizado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Dados inválidos, JSON inválido ou e-mail já cadastrado
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
                                            name = "dadosInvalidos",
                                            summary = "Campos enviados são inválidos",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": {
                                                        "email": "E-mail inválido",
                                                        "senha": "Senha deve ter entre 6 e 100 caracteres"
                                                      },
                                                      "mensagem": "Dados inválidos"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "emailJaCadastrado",
                                            summary = "E-mail já utilizado",
                                            value = """
                                                    {
                                                      "sucesso": false,
                                                      "dados": null,
                                                      "mensagem": "E-mail já cadastrado"
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
                    description = "Conflito de integridade no banco de dados",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaErroOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "violacaoIntegridade",
                                    summary = "Violação de integridade dos dados",
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
    @PatchMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.usuario.id or hasRole('ADMIN')")
    public RespostaApi<UsuarioResponseDTO> atualizarUsuario(
            @Parameter(
                    description = "Identificador do usuário",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAtualizacaoRequest usuarioAtualizacaoRequest
    ) {
        return RespostaApi.sucesso(usuarioService.atualizarUsuario(id, usuarioAtualizacaoRequest),
                "Usuário atualizado");
    }

}
