package br.com.api.pedidos.auth.controller;

import br.com.api.pedidos.auth.dto.LoginRequestDTO;
import br.com.api.pedidos.auth.dto.LoginResponseDTO;
import br.com.api.pedidos.auth.dto.RefreshTokenRequestDTO;
import br.com.api.pedidos.auth.dto.RegistraRequestDTO;
import br.com.api.pedidos.auth.dto.openapi.RespostaLoginOpenApiDTO;
import br.com.api.pedidos.auth.service.AutenticacaoService;
import br.com.api.pedidos.auth.service.RegistroUsuarioService;
import br.com.api.pedidos.security.util.RequestUtils;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroOpenApiDTO;
import br.com.api.pedidos.shared.openapi.dto.RespostaErroValidacaoOpenApiDTO;
import br.com.api.pedidos.shared.openapi.dto.RespostaSucessoSemDadosOpenApiDTO;
import br.com.api.pedidos.shared.response.RespostaApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Autenticação",
        description = """
                Endpoints públicos de login, registro e renovação,
                além do logout autenticado
                """
)
@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;
    private final RegistroUsuarioService registroUsuarioService;

    public AutenticacaoController(
            AutenticacaoService autenticacaoService,
            RegistroUsuarioService registroUsuarioService) {
        this.autenticacaoService = autenticacaoService;
        this.registroUsuarioService = registroUsuarioService;
    }

    @Operation(
            summary = "Realizar Login",
            description = """
                Autentica um usuário ativo utilizando e-mail e senha.

                Quando as credenciais são válidas, retorna um access token
                JWT e um refresh token.

                O access token deve ser informado no botão Authorize para
                acessar os endpoints protegidos.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaLoginOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "loginRealizado",
                                    summary = "Autenticação realizada com sucesso",
                                    value = """
                                        {
                                          "sucesso": true,
                                          "dados": {
                                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.token-jwt-ficticio",
                                            "refreshToken": "refresh-token-ficticio"
                                          },
                                          "mensagem": "Login realizado com sucesso"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                        Dados inválidos, senha incorreta, usuário desativado
                        ou usuário temporariamente bloqueado
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
                                            name = "senhaIncorreta",
                                            summary = "Senha informada está incorreta",
                                            value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Senha incorreta"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "usuarioDesativado",
                                            summary = "Usuário está desativado",
                                            value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Usuário desativado"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "usuarioBloqueado",
                                            summary = "Usuário temporariamente bloqueado",
                                            value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Usuário bloqueado temporariamente"
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
                                    summary = "Usuário não encontrado",
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
    @PostMapping("/login")
    public RespostaApi<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO,
                                               HttpServletRequest request) {
        String requestIp = RequestUtils.extrairIp(request);
        String userAgent = RequestUtils.extrairUserAgent(request);

        return RespostaApi.sucesso(
                autenticacaoService.login(
                        loginRequestDTO,
                        requestIp,
                        userAgent),
                "Login realizado com sucesso");
    }

    @Operation(
            summary = "Renovar autenticação",
            description = """
                Recebe um refresh token válido e retorna um novo access token
                e um novo refresh token.

                O refresh token utilizado é revogado, implementando rotação
                de refresh tokens.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens renovados com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = RespostaLoginOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "tokensRenovados",
                                    summary = "Tokens renovados com sucesso",
                                    value = """
                                        {
                                          "sucesso": true,
                                          "dados": {
                                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.novo-token-jwt-ficticio",
                                            "refreshToken": "novo-refresh-token-ficticio"
                                          },
                                          "mensagem": "Tokens renovados com sucesso"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                        Refresh token ausente, inválido, expirado ou revogado
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
                                            name = "refreshTokenAusente",
                                            summary = "Refresh token não informado",
                                            value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": {
                                                    "refreshToken": "Refresh token é obrigatório"
                                                  },
                                                  "mensagem": "Dados inválidos"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "refreshTokenInvalido",
                                            summary = "Refresh token inválido",
                                            value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Token inválido"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "refreshTokenExpirado",
                                            summary = "Refresh token expirado",
                                            value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Refresh token expirado"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "refreshTokenRevogado",
                                            summary = "Refresh token revogado reutilizado",
                                            value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Possível roubo de refresh token detectado. Faça login novamente."
                                                }
                                                """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/refresh")
    public RespostaApi<LoginResponseDTO> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO,
            HttpServletRequest request) {
        String requestIp = RequestUtils.extrairIp(request);
        String userAgent = RequestUtils.extrairUserAgent(request);

        return RespostaApi.sucesso(autenticacaoService.refresh(
                refreshTokenRequestDTO,
                requestIp,
                userAgent),
                "Tokens renovados com sucesso");
    }

    @Operation(
            summary = "Registrar usuário",
            description = """
                Cria um novo usuário com o perfil USER.

                A senha recebida não é armazenada em texto puro. Ela é
                codificada antes da persistência.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário registrado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            RespostaSucessoSemDadosOpenApiDTO.class
                            ),
                            examples = @ExampleObject(
                                    name = "usuarioRegistrado",
                                    summary = "Usuário registrado com sucesso",
                                    value = """
                                        {
                                          "sucesso": true,
                                          "dados": null,
                                          "mensagem": "Usuário registrado com sucesso"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                        Dados de entrada inválidos ou usuário já cadastrado
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
                                            name = "dadosRegistroInvalidos",
                                            summary = "Dados de registro inválidos",
                                            value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": {
                                                    "nome": "Nome é obrigatório",
                                                    "email": "E-mail inválido",
                                                    "senha": "Senha deve ter entre 6 e 100 caracteres"
                                                  },
                                                  "mensagem": "Dados inválidos"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "usuarioJaExiste",
                                            summary = "E-mail já cadastrado",
                                            value = """
                                                {
                                                  "sucesso": false,
                                                  "dados": null,
                                                  "mensagem": "Usuário já existe"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "jsonRegistroInvalido",
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
                                    name = "conflitoIntegridade",
                                    summary = "Conflito de integridade dos dados",
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
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registrar")
    public RespostaApi<Void> registrarUsuario(
            @Valid @RequestBody RegistraRequestDTO registraRequestDTO) {
        registroUsuarioService.registrar(registraRequestDTO);
        return RespostaApi.sucesso(null,
                "Usuário registrado com sucesso");
    }

    @Operation(
            summary = "Realizar logout",
            description = """
                Invalida o access token utilizado na requisição e revoga
                todos os refresh tokens do usuário.

                Este endpoint exige autenticação Bearer JWT.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout realizado com sucesso",
                    content = @Content
            ),
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
            )
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        autenticacaoService.logout(request.getHeader("Authorization"));
    }

}
