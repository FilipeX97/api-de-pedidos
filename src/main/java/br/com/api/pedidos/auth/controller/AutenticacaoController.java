package br.com.api.pedidos.auth.controller;

import br.com.api.pedidos.auth.dto.LoginRequestDTO;
import br.com.api.pedidos.auth.dto.LoginResponseDTO;
import br.com.api.pedidos.auth.dto.RefreshTokenRequestDTO;
import br.com.api.pedidos.auth.dto.RegistraRequestDTO;
import br.com.api.pedidos.auth.service.AutenticacaoService;
import br.com.api.pedidos.auth.service.TokenBlacklistService;
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
    public LoginResponseDTO login(@RequestBody LoginRequestDTO loginRequestDTO) {
        return autenticacaoService.login(loginRequestDTO);
    }

    @PostMapping("/refresh")
    public LoginResponseDTO refresh(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return autenticacaoService.refresh(refreshTokenRequestDTO);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registrar")
    public void registrarUsuario(@RequestBody RegistraRequestDTO registraRequestDTO) {
        autenticacaoService.registrarUsuario(registraRequestDTO);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return;
        }

        String token = header.substring(7);
        tokenBlacklistService.adicionarBlacklist(token);
    }

}
