package br.com.api.pedidos.auth.service;

import br.com.api.pedidos.auth.entity.RefreshToken;
import br.com.api.pedidos.auth.repository.RefreshTokenRepository;
import br.com.api.pedidos.config.TokenProperties;
import br.com.api.pedidos.shared.exception.RegraNegocioException;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

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

    @Transactional
    public String criar(Usuario usuario) {
        String token = gerarTokenSeguro();

        RefreshToken refreshToken = new RefreshToken(
                token,
                Instant.now().plusMillis(tokenProperties.expiracaoRefresh()),
                usuario
        );

        refreshTokenRepository.saveAndFlush(refreshToken);
        return token;
    }

    @Transactional(readOnly = true)
    public RefreshToken buscar(String token) {
        return refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new RegraNegocioException("Token inválido"));
    }

    @Transactional
    public void revogar(RefreshToken token) {
        token.revogar();
        refreshTokenRepository.save(token);
    }

    public void detectarUsoDeTokenRevogado(RefreshToken token) {
        if (token.isRevogado()) {
            Usuario usuario = token.getUsuario();
            revogarTodosTokensUsuario(usuario);

            throw new RegraNegocioException(
                    "Possível roubo de refresh token detectado. Faça login novamente."
            );
        }
    }

    @Transactional
    public void revogarTodosTokensUsuario(Usuario usuario) {
        refreshTokenRepository.deleteAllByUsuario(usuario);
    }

    public void validarExpiracao(RefreshToken token) {
        if (token.getExpiration().isBefore(Instant.now())) {
            throw new RegraNegocioException("Refresh token expirado");
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
