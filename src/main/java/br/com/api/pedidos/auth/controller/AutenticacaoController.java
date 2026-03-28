package br.com.api.pedidos.auth.controller;

import br.com.api.pedidos.auth.dto.LoginRequestDTO;
import br.com.api.pedidos.auth.dto.LoginResponseDTO;
import br.com.api.pedidos.auth.dto.RefreshTokenRequestDTO;
import br.com.api.pedidos.auth.dto.RegistraRequestDTO;
import br.com.api.pedidos.auth.service.AutenticacaoService;
import br.com.api.pedidos.auth.service.TokenBlacklistService;
import br.com.api.pedidos.shared.response.RespostaApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;
    private final TokenBlacklistService tokenBlacklistService;

    public AutenticacaoController(
            AutenticacaoService autenticacaoService, TokenBlacklistService tokenBlacklistService) {
        this.autenticacaoService = autenticacaoService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public RespostaApi<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        return RespostaApi.sucesso(autenticacaoService.login(loginRequestDTO),
                "Login realizado com sucesso");
    }

    @PostMapping("/refresh")
    public RespostaApi<LoginResponseDTO> refresh(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return RespostaApi.sucesso(autenticacaoService.refresh(refreshTokenRequestDTO),
                "Token renovado com sucesso");
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registrar")
    public RespostaApi<Void> registrarUsuario(@RequestBody RegistraRequestDTO registraRequestDTO) {
        autenticacaoService.registrarUsuario(registraRequestDTO);
        return RespostaApi.sucesso(null,
                "Usuário registrado com sucesso");
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    public RespostaApi<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            tokenBlacklistService.adicionarBlacklist(token);
        }

        return RespostaApi.sucesso(null, "Logout realizado com sucesso");
    }

}
