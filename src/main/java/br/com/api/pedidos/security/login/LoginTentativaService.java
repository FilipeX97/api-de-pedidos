package br.com.api.pedidos.security.login;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginTentativaService {

    private static final int MAX_TENTATIVAS = 5;

    private final Cache<String, Integer> tentativas;
    private final Cache<String, Boolean> bloqueios;

    public LoginTentativaService() {
        this.tentativas = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();

        this.bloqueios = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    public void registrarFalha(String email, String ip) {
        String chave = gerarChave(email, ip);
        Integer tentativa = tentativas.getIfPresent(chave);

        if (tentativa == null) {
            tentativa = 0;
        }

        tentativa++;
        tentativas.put(chave, tentativa);

        if (tentativa >= MAX_TENTATIVAS) {
            bloqueios.put(chave, true);
        }
    }

    public boolean estaBloqueado(String email, String ip) {
        String chave = gerarChave(email, ip);
        return bloqueios.getIfPresent(chave) != null;
    }

    public void sucessoLogin(String email, String ip) {
        String chave = gerarChave(email, ip);
        tentativas.invalidate(chave);
        bloqueios.invalidate(chave);
    }

    private String gerarChave(String email, String ip) {
        return email + ":" + ip;
    }

}
