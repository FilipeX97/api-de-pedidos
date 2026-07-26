package br.com.api.pedidos.coupon.entity;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CupomTest {

    @Nested
    class CriacaoDoCupom {

        @Test
        void deveCriarCupomValido() {
            LocalDateTime inicio = LocalDateTime.now().minusDays(1);
            LocalDateTime fim = LocalDateTime.now().plusDays(10);

            Cupom cupom = new Cupom(
                    "promo10",
                    new BigDecimal("0.10"),
                    inicio,
                    fim,
                    100
            );

            assertAll(
                    () -> assertEquals("PROMO10", cupom.getCodigo()),
                    () -> assertEquals(
                            new BigDecimal("0.10"),
                            cupom.getPercentual()
                    ),
                    () -> assertEquals(inicio, cupom.getDataInicio()),
                    () -> assertEquals(fim, cupom.getDataFim()),
                    () -> assertEquals(100, cupom.getLimiteUso()),
                    () -> assertEquals(0, cupom.getQuantidadeDeUso()),
                    () -> assertTrue(cupom.isAtivo()),
                    () -> assertTrue(cupom.podeSerUtilizado())
            );
        }

        @Test
        void naoDeveCriarCupomComCodigoNulo() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Cupom(
                            null,
                            new BigDecimal("0.10"),
                            LocalDateTime.now().minusDays(1),
                            LocalDateTime.now().plusDays(1),
                            10
                    )
            );

            assertEquals(
                    "Código do cupom é obrigatório",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarCupomComCodigoVazio() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Cupom(
                            "   ",
                            new BigDecimal("0.10"),
                            LocalDateTime.now().minusDays(1),
                            LocalDateTime.now().plusDays(1),
                            10
                    )
            );

            assertEquals(
                    "Código do cupom é obrigatório",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarCupomComPercentualNulo() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Cupom(
                            "PROMO10",
                            null,
                            LocalDateTime.now().minusDays(1),
                            LocalDateTime.now().plusDays(1),
                            10
                    )
            );

            assertEquals(
                    "Percentual do cupom deve ser entre 0 e 1",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarCupomComPercentualZero() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Cupom(
                            "PROMOZERO",
                            BigDecimal.ZERO,
                            LocalDateTime.now().minusDays(1),
                            LocalDateTime.now().plusDays(1),
                            10
                    )
            );

            assertEquals(
                    "Percentual do cupom deve ser entre 0 e 1",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarCupomComPercentualNegativo() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Cupom(
                            "PROMONEGATIVO",
                            new BigDecimal("-0.10"),
                            LocalDateTime.now().minusDays(1),
                            LocalDateTime.now().plusDays(1),
                            10
                    )
            );

            assertEquals(
                    "Percentual do cupom deve ser entre 0 e 1",
                    excecao.getMessage()
            );
        }

        @Test
        void devePermitirPercentualIgualAUm() {
            Cupom cupom = new Cupom(
                    "GRATIS",
                    BigDecimal.ONE,
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(1),
                    10
            );

            assertEquals(BigDecimal.ONE, cupom.getPercentual());
        }

        @Test
        void naoDeveCriarCupomComPercentualMaiorQueUm() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Cupom(
                            "PROMO150",
                            new BigDecimal("1.50"),
                            LocalDateTime.now().minusDays(1),
                            LocalDateTime.now().plusDays(1),
                            10
                    )
            );

            assertEquals(
                    "Percentual do cupom deve ser entre 0 e 1",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarCupomSemDataInicial() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Cupom(
                            "PROMO10",
                            new BigDecimal("0.10"),
                            null,
                            LocalDateTime.now().plusDays(1),
                            10
                    )
            );

            assertEquals(
                    "Período do cupom é obrigatório",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarCupomSemDataFinal() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Cupom(
                            "PROMO10",
                            new BigDecimal("0.10"),
                            LocalDateTime.now().minusDays(1),
                            null,
                            10
                    )
            );

            assertEquals(
                    "Período do cupom é obrigatório",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarCupomComDataFinalAnteriorAInicial() {
            LocalDateTime inicio = LocalDateTime.now().plusDays(5);
            LocalDateTime fim = inicio.minusDays(1);

            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Cupom(
                            "PROMO10",
                            new BigDecimal("0.10"),
                            inicio,
                            fim,
                            10
                    )
            );

            assertEquals(
                    "Data final não pode ser anterior à data inicial",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarCupomComLimiteDeUsoNulo() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Cupom(
                            "PROMO10",
                            new BigDecimal("0.10"),
                            LocalDateTime.now().minusDays(1),
                            LocalDateTime.now().plusDays(1),
                            null
                    )
            );

            assertEquals(
                    "Limite de uso deve ser maior que zero",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarCupomComLimiteDeUsoZero() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Cupom(
                            "PROMO10",
                            new BigDecimal("0.10"),
                            LocalDateTime.now().minusDays(1),
                            LocalDateTime.now().plusDays(1),
                            0
                    )
            );

            assertEquals(
                    "Limite de uso deve ser maior que zero",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveCriarCupomComLimiteDeUsoNegativo() {
            IllegalArgumentException excecao = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Cupom(
                            "PROMO10",
                            new BigDecimal("0.10"),
                            LocalDateTime.now().minusDays(1),
                            LocalDateTime.now().plusDays(1),
                            -1
                    )
            );

            assertEquals(
                    "Limite de uso deve ser maior que zero",
                    excecao.getMessage()
            );
        }
    }

    @Nested
    class UtilizacaoDoCupom {

        @Test
        void deveRegistrarUsoDoCupom() {
            Cupom cupom = criarCupomValido(2);

            cupom.registrarUso();

            assertAll(
                    () -> assertEquals(1, cupom.getQuantidadeDeUso()),
                    () -> assertTrue(cupom.podeSerUtilizado())
            );
        }

        @Test
        void naoDeveUtilizarCupomExpirado() {
            Cupom cupom = new Cupom(
                    "EXPIRADO",
                    new BigDecimal("0.10"),
                    LocalDateTime.now().minusDays(10),
                    LocalDateTime.now().minusDays(1),
                    10
            );

            assertFalse(cupom.podeSerUtilizado());

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    cupom::registrarUso
            );

            assertEquals(
                    "Cupom indisponível",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveUtilizarCupomAntesDaDataInicial() {
            Cupom cupom = new Cupom(
                    "FUTURO",
                    new BigDecimal("0.10"),
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(10),
                    10
            );

            assertFalse(cupom.podeSerUtilizado());

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    cupom::registrarUso
            );

            assertEquals(
                    "Cupom indisponível",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveUtilizarCupomAcimaDoLimite() {
            Cupom cupom = criarCupomValido(1);

            cupom.registrarUso();

            assertEquals(1, cupom.getQuantidadeDeUso());
            assertFalse(cupom.podeSerUtilizado());

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    cupom::registrarUso
            );

            assertEquals(
                    "Cupom indisponível",
                    excecao.getMessage()
            );

            assertEquals(
                    1,
                    cupom.getQuantidadeDeUso(),
                    "A quantidade não deve aumentar quando o limite foi atingido"
            );
        }

        @Test
        void naoDeveUtilizarCupomDesativado() {
            Cupom cupom = criarCupomValido(10);

            cupom.desativar();

            assertFalse(cupom.isAtivo());
            assertFalse(cupom.podeSerUtilizado());

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    cupom::registrarUso
            );

            assertEquals(
                    "Cupom indisponível",
                    excecao.getMessage()
            );
        }
    }

    @Nested
    class AtivacaoDoCupom {

        @Test
        void deveDesativarCupomAtivo() {
            Cupom cupom = criarCupomValido(10);

            cupom.desativar();

            assertFalse(cupom.isAtivo());
        }

        @Test
        void deveAtivarCupomDesativado() {
            Cupom cupom = criarCupomValido(10);
            cupom.desativar();

            cupom.ativar();

            assertTrue(cupom.isAtivo());
        }

        @Test
        void naoDeveAtivarCupomQueJaEstaAtivo() {
            Cupom cupom = criarCupomValido(10);

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    cupom::ativar
            );

            assertEquals(
                    "Cupom já está ativo",
                    excecao.getMessage()
            );
        }

        @Test
        void naoDeveDesativarCupomQueJaEstaDesativado() {
            Cupom cupom = criarCupomValido(10);
            cupom.desativar();

            IllegalStateException excecao = assertThrows(
                    IllegalStateException.class,
                    cupom::desativar
            );

            assertEquals(
                    "Cupom já está desativado",
                    excecao.getMessage()
            );
        }
    }

    private Cupom criarCupomValido(Integer limiteUso) {
        return new Cupom(
                "PROMO10",
                new BigDecimal("0.10"),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                limiteUso
        );
    }
}