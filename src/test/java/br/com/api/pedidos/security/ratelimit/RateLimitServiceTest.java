package br.com.api.pedidos.security.ratelimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RateLimitServiceTest {

    @Test
    void devePermitirPrimeiraRequisicaoEBloquearSegundaImediata() {
        RateLimitService rateLimitService = new RateLimitService();

        boolean primeira = rateLimitService.permitirRequisicao("USER:1");
        boolean segunda = rateLimitService.permitirRequisicao("USER:1");

        assertTrue(primeira);
        assertFalse(segunda);
    }
}
