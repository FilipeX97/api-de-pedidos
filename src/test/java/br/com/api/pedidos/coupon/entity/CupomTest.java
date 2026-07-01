package br.com.api.pedidos.coupon.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CupomTest {

    @Test
    void deveCriarCupomValido() {
        Cupom cupom = new Cupom(
                "PROMO10",
                BigDecimal.valueOf(0.10),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                100
        );

        assertEquals("PROMO10", cupom.getCodigo());
        assertTrue(cupom.isAtivo());
        assertEquals(0, cupom.getQuantidadeDeUso());
        assertTrue(cupom.podeSerUtilizado());
    }

    @Test
    void naoDeveCriarCupomComPercentualMaiorQueUm() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Cupom(
                        "PROMO150",
                        BigDecimal.valueOf(1.50),
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10),
                        100
                )
        );
    }

    @Test
    void naoDeveCriarCupomComDataFinalAntesDaInicial() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Cupom(
                        "PROMO",
                        BigDecimal.valueOf(0.10),
                        LocalDateTime.now().plusDays(10),
                        LocalDateTime.now().minusDays(1),
                        100
                )
        );
    }

    @Test
    void deveRegistrarUsoDoCupom() {
        Cupom cupom = new Cupom(
                "PROMO10",
                BigDecimal.valueOf(0.10),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                2
        );

        cupom.registrarUso();

        assertEquals(1, cupom.getQuantidadeDeUso());
    }

    @Test
    void naoDeveUsarCupomAcimaDoLimite() {
        Cupom cupom = new Cupom(
                "PROMO10",
                BigDecimal.valueOf(0.10),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                1
        );

        cupom.registrarUso();

        assertThrows(
                IllegalStateException.class,
                cupom::registrarUso
        );
    }
}