package br.com.api.pedidos.auth.service;

import br.com.api.pedidos.auth.entity.RefreshToken;
import br.com.api.pedidos.auth.repository.RefreshTokenRepository;
import br.com.api.pedidos.config.TokenProperties;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProperties tokenProperties;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            TokenProperties tokenProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProperties = tokenProperties;
    }

    public String criar(Usuario usuario) {
        String token = gerarTokenSeguro();

        refreshTokenRepository.save(
                new RefreshToken(
                        token,
                        new Date(
                                System.currentTimeMillis()
                                        + tokenProperties.expiracaoRefresh()),
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

    public void detectarUsoDeTokenRevogado(RefreshToken token) {
        if (token.isRevogado()) {
            Usuario usuario = token.getUsuario();
            revogarTodosTokensUsuario(usuario);

            throw new RuntimeException(
                    "Possível roubo de refresh token detectado. Faça login novamente."
            );
        }
    }

    public void revogarTodosTokensUsuario(Usuario usuario) {
        refreshTokenRepository.deleteAllByUsuario(usuario);
    }

    public void validarExpiracao(RefreshToken token) {
        if (token.getExpiration().before(new Date())) {
            throw new RuntimeException("Refresh token expirado");
        }
    }

    private String gerarTokenSeguro() {
        byte[] bytes = new byte[64];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

}
