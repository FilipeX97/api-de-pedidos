package br.com.api.pedidos.security.login;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginTentativaService {

    private final Map<String, Integer> tentativas = new ConcurrentHashMap<>();
    private final Map<String, Long> bloqueios = new ConcurrentHashMap<>();

    private static final int MAX_TENTATIVAS = 5;
    private static final long BLOQUEIO = 10 * 60 * 1000; // 10 minutos

    public void registrarFalha(String email) {
        int tentativa = tentativas.getOrDefault(email, 0);
        tentativa++;
        tentativas.put(email, tentativa);

        if (tentativa >= MAX_TENTATIVAS) {
            bloqueios.put(email, System.currentTimeMillis());
        }
    }

    public boolean estaBloqueado(String email) {
        Long bloqueio = bloqueios.get(email);

        if (bloqueio == null) {
            return false;
        }

        if ((System.currentTimeMillis() - bloqueio) > BLOQUEIO) {
            bloqueios.remove(email);
            tentativas.remove(email);
            return false;
        }

        return true;
    }

    public void sucessoLogin(String email) {
        tentativas.remove(email);
        bloqueios.remove(email);
    }

}
