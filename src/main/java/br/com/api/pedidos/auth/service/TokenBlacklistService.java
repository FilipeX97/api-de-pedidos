package br.com.api.pedidos.auth.service;

import br.com.api.pedidos.auth.entity.BlacklistedToken;
import br.com.api.pedidos.auth.repository.BlacklistedTokenRepository;
import br.com.api.pedidos.security.jwt.JwtService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void adicionarBlacklist(String token) {
        Instant expiracao = jwtService.extrairExpiracao(token);

        BlacklistedToken blacklistedToken = new BlacklistedToken(
                token,
                expiracao
        );

        blacklistedTokenRepository.saveAndFlush(blacklistedToken);
    }

    @Cacheable(value = "blacklist", key = "#token")
    public boolean tokenBloqueado(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

}
