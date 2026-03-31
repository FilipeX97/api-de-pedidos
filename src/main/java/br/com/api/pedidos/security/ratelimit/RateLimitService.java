package br.com.api.pedidos.security.ratelimit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Long> ultimaRequisicao = new ConcurrentHashMap<>();

    private static final long INTERVALO_MINIMO = 1000; // 1 segundo
    private static final long TEMPO_LIMPEZA = 60000; // 1 minuto

    public boolean permitirRequisicao(String chave) {
        long agora = System.currentTimeMillis();
        Long ultima = ultimaRequisicao.get(chave);

        if(ultima != null && (agora - ultima) < INTERVALO_MINIMO) {
            return false;
        }

        ultimaRequisicao.put(chave, agora);
        return true;
    }

    @Scheduled(fixedRate = 60000)
    public void limparCache() {
        long agora = System.currentTimeMillis();
        ultimaRequisicao.entrySet().removeIf(
                entry -> (agora - entry.getValue()) > TEMPO_LIMPEZA);
    }

}
