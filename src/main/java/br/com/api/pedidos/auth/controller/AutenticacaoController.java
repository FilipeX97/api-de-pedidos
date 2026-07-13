package br.com.api.pedidos.auth.controller;

import br.com.api.pedidos.auth.dto.LoginRequestDTO;
import br.com.api.pedidos.auth.dto.LoginResponseDTO;
import br.com.api.pedidos.auth.dto.RefreshTokenRequestDTO;
import br.com.api.pedidos.auth.dto.RegistraRequestDTO;
import br.com.api.pedidos.auth.service.AutenticacaoService;
import br.com.api.pedidos.auth.service.RegistroUsuarioService;
import br.com.api.pedidos.security.util.RequestUtils;
import br.com.api.pedidos.shared.response.RespostaApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
                "Token renovado com sucesso");
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registrar")
    public RespostaApi<Void> registrarUsuario(@Valid @RequestBody RegistraRequestDTO registraRequestDTO) {
        registroUsuarioService.registrar(registraRequestDTO);
        return RespostaApi.sucesso(null,
                "Usuário registrado com sucesso");
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    public RespostaApi<Void> logout(HttpServletRequest request) {
        autenticacaoService.logout(
                request.getHeader("Authorization")
        );

        return RespostaApi.sucesso(null, "Logout realizado com sucesso");
    }

}
