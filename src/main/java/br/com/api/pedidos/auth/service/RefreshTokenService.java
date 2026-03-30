package br.com.api.pedidos.auth.service;

import br.com.api.pedidos.auth.entity.RefreshToken;
import br.com.api.pedidos.auth.repository.RefreshTokenRepository;
import br.com.api.pedidos.config.TokenProperties;
import br.com.api.pedidos.security.jwt.JwtService;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final TokenProperties tokenProperties;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            TokenProperties tokenProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.tokenProperties = tokenProperties;
    }

    public String criar(Usuario usuario) {
        String token = jwtService.gerarRefreshToken(usuario);

        refreshTokenRepository.save(
                new RefreshToken(
                        token,
                        new Date(System.currentTimeMillis() + tokenProperties.expiracaoRefresh()),
                        usuario
                )
        );

        return token;
    }

    public RefreshToken buscar(String token) {
        return refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));
    }

    public void revogar(RefreshToken token) {
        token.revogar();
        refreshTokenRepository.save(token);
    }

}
