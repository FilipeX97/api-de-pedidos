package br.com.api.pedidos.auth.service;

import br.com.api.pedidos.auth.entity.BlacklistedToken;
import br.com.api.pedidos.auth.repository.BlacklistedTokenRepository;
import br.com.api.pedidos.security.jwt.JwtService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtService jwtService;

    public TokenBlacklistService(
            BlacklistedTokenRepository blacklistedTokenRepository,
            JwtService jwtService) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.jwtService = jwtService;
    }

    public void adicionarBlacklist(String token) {
        Instant expiracao = jwtService.extrairExpiracao(token);

        blacklistedTokenRepository.save(
                new BlacklistedToken(token, expiracao)
        );
    }

    public boolean tokenBloqueado(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

}
