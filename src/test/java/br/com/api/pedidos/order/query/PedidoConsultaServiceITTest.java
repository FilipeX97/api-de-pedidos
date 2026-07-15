package br.com.api.pedidos.order.query;

import br.com.api.pedidos.coupon.entity.Cupom;
import br.com.api.pedidos.coupon.repository.CupomRepository;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.query.dto.PedidoFiltroDTO;
import br.com.api.pedidos.order.query.service.PedidoConsultaService;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.order.state.StatusPedido;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=" +
                "jdbc:h2:mem:pedido_consulta_it;" +
                "DB_CLOSE_DELAY=-1;" +
                "DB_CLOSE_ON_EXIT=FALSE"
})
class PedidoConsultaServiceITTest {

    @Autowired
    private PedidoConsultaService pedidoConsultaService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CupomRepository cupomRepository;

    @Test
    void deveFiltrarPorStatus() {
        Usuario usuario = criarUsuario();

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                LocalDateTime.of(2026, 7, 10, 10, 0),
                new BigDecimal("100.00"),
                null
        );

        criarPedido(
                usuario,
                StatusPedido.CANCELADO,
                LocalDateTime.of(2026, 7, 10, 11, 0),
                new BigDecimal("200.00"),
                null
        );

        var resultado = pedidoConsultaService.consultar(
                filtro(
                        StatusPedido.PAGO,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                PageRequest.of(0, 20)
        );

        assertEquals(1L, resultado.totalElementos());
        assertEquals(
                StatusPedido.PAGO,
                resultado.conteudo().getFirst().status()
        );
    }

    @Test
    void deveFiltrarPorUsuario() {
        Usuario usuarioUm = criarUsuario();
        Usuario usuarioDois = criarUsuario();

        criarPedido(
                usuarioUm,
                StatusPedido.PAGO,
                LocalDateTime.now(),
                new BigDecimal("100.00"),
                null
        );

        criarPedido(
                usuarioDois,
                StatusPedido.PAGO,
                LocalDateTime.now(),
                new BigDecimal("200.00"),
                null
        );

        var resultado = pedidoConsultaService.consultar(
                filtro(
                        null,
                        usuarioUm.getId(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                PageRequest.of(0, 20)
        );

        assertEquals(1L, resultado.totalElementos());
        assertEquals(
                usuarioUm.getEmail(),
                resultado.conteudo().getFirst().emailCliente()
        );
    }

    @Test
    void deveFiltrarPorEmail() {
        Usuario usuarioUm = criarUsuario();
        Usuario usuarioDois = criarUsuario();

        criarPedido(
                usuarioUm,
                StatusPedido.PAGO,
                LocalDateTime.now(),
                new BigDecimal("100.00"),
                null
        );

        criarPedido(
                usuarioDois,
                StatusPedido.PAGO,
                LocalDateTime.now(),
                new BigDecimal("200.00"),
                null
        );

        var resultado = pedidoConsultaService.consultar(
                filtro(
                        null,
                        null,
                        usuarioUm.getEmail().toUpperCase(),
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                PageRequest.of(0, 20)
        );

        assertEquals(1L, resultado.totalElementos());
        assertEquals(
                usuarioUm.getEmail(),
                resultado.conteudo().getFirst().emailCliente()
        );
    }

    @Test
    void deveFiltrarPorPeriodo() {
        Usuario usuario = criarUsuario();

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                LocalDateTime.of(2026, 6, 30, 23, 59),
                new BigDecimal("50.00"),
                null
        );

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                LocalDateTime.of(2026, 7, 15, 12, 0),
                new BigDecimal("100.00"),
                null
        );

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                new BigDecimal("150.00"),
                null
        );

        var resultado = pedidoConsultaService.consultar(
                filtro(
                        null,
                        null,
                        usuario.getEmail(),
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 31),
                        null,
                        null,
                        null
                ),
                PageRequest.of(0, 20)
        );

        assertEquals(1L, resultado.totalElementos());
        assertEquals(
                new BigDecimal("100.00"),
                resultado.conteudo().getFirst().valorFinal()
        );
    }

    @Test
    void deveIncluirTodoODiaFinal() {
        Usuario usuario = criarUsuario();

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                LocalDateTime.of(
                        2026,
                        7,
                        31,
                        23,
                        59,
                        59
                ),
                new BigDecimal("100.00"),
                null
        );

        var resultado = pedidoConsultaService.consultar(
                filtro(
                        null,
                        null,
                        usuario.getEmail(),
                        LocalDate.of(2026, 7, 31),
                        LocalDate.of(2026, 7, 31),
                        null,
                        null,
                        null
                ),
                PageRequest.of(0, 20)
        );

        assertEquals(1L, resultado.totalElementos());
    }

    @Test
    void deveFiltrarPorCupom() {
        Usuario usuario = criarUsuario();
        Cupom cupom = criarCupom("TESTE10");

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                LocalDateTime.now(),
                new BigDecimal("100.00"),
                cupom
        );

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                LocalDateTime.now(),
                new BigDecimal("200.00"),
                null
        );

        var resultado = pedidoConsultaService.consultar(
                filtro(
                        null,
                        null,
                        usuario.getEmail(),
                        null,
                        null,
                        null,
                        null,
                        "teste10"
                ),
                PageRequest.of(0, 20)
        );

        assertEquals(1L, resultado.totalElementos());
        assertEquals(
                "TESTE10",
                resultado.conteudo().getFirst().codigoCupom()
        );
    }

    @Test
    void deveFiltrarPorValorMinimoEMaximo() {
        Usuario usuario = criarUsuario();

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                LocalDateTime.now(),
                new BigDecimal("50.00"),
                null
        );

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                LocalDateTime.now(),
                new BigDecimal("150.00"),
                null
        );

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                LocalDateTime.now(),
                new BigDecimal("600.00"),
                null
        );

        var resultado = pedidoConsultaService.consultar(
                filtro(
                        null,
                        null,
                        usuario.getEmail(),
                        null,
                        null,
                        new BigDecimal("100.00"),
                        new BigDecimal("500.00"),
                        null
                ),
                PageRequest.of(0, 20)
        );

        assertEquals(1L, resultado.totalElementos());
        assertEquals(
                new BigDecimal("150.00"),
                resultado.conteudo().getFirst().valorFinal()
        );
    }

    @Test
    void deveCombinarVariosFiltros() {
        Usuario usuario = criarUsuario();
        Cupom cupom = criarCupom("COMBINADO20");

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                LocalDateTime.of(2026, 7, 10, 12, 0),
                new BigDecimal("250.00"),
                cupom
        );

        criarPedido(
                usuario,
                StatusPedido.CANCELADO,
                LocalDateTime.of(2026, 7, 10, 12, 0),
                new BigDecimal("250.00"),
                cupom
        );

        var resultado = pedidoConsultaService.consultar(
                filtro(
                        StatusPedido.PAGO,
                        usuario.getId(),
                        usuario.getEmail(),
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 31),
                        new BigDecimal("200.00"),
                        new BigDecimal("300.00"),
                        "COMBINADO20"
                ),
                PageRequest.of(
                        0,
                        20,
                        Sort.by("dataCriacao")
                )
        );

        assertEquals(1L, resultado.totalElementos());
        assertEquals(
                StatusPedido.PAGO,
                resultado.conteudo().getFirst().status()
        );
    }

    @Test
    void deveRejeitarPeriodoInvertido() {
        PedidoFiltroDTO filtro = filtro(
                null,
                null,
                null,
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 7, 1),
                null,
                null,
                null
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> pedidoConsultaService.consultar(
                                filtro,
                                PageRequest.of(0, 20)
                        )
                );

        assertEquals(
                "Data inicial não pode ser posterior à data final",
                exception.getMessage()
        );
    }

    @Test
    void deveRejeitarValoresInvertidos() {
        PedidoFiltroDTO filtro = filtro(
                null,
                null,
                null,
                null,
                null,
                new BigDecimal("500.00"),
                new BigDecimal("100.00"),
                null
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> pedidoConsultaService.consultar(
                                filtro,
                                PageRequest.of(0, 20)
                        )
                );

        assertEquals(
                "Valor mínimo não pode ser maior que o valor máximo",
                exception.getMessage()
        );
    }

    @Test
    void deveRejeitarPaginaMaiorQueCemRegistros() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> pedidoConsultaService.consultar(
                                filtroVazio(),
                                PageRequest.of(0, 101)
                        )
                );

        assertEquals("Tamanho máximo da página é 100",exception.getMessage());
    }

    @Test
    void deveRejeitarCampoDeOrdenacaoInexistente() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> pedidoConsultaService.consultar(
                                filtroVazio(),
                                PageRequest.of(
                                        0,
                                        20,
                                        Sort.by("campoInexistente")
                                )
                        )
                );

        assertTrue(exception.getMessage().contains("Campo de ordenação inválido"));
    }

    private Usuario criarUsuario() {
        String identificador = UUID.randomUUID().toString();

        return usuarioRepository.saveAndFlush(
                new Usuario(
                        "Usuário " + identificador,
                        identificador + "@teste.com",
                        "123456",
                        Perfil.USER
                )
        );
    }

    private Cupom criarCupom(String codigo) {
        return cupomRepository.saveAndFlush(
                new Cupom(
                        codigo,
                        new BigDecimal("0.10"),
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(30),
                        100
                )
        );
    }

    private Pedido criarPedido(
            Usuario usuario,
            StatusPedido status,
            LocalDateTime dataCriacao,
            BigDecimal valorFinal,
            Cupom cupom
    ) {
        Pedido pedido = new Pedido(usuario);

        ReflectionTestUtils.setField(
                pedido,
                "status",
                status
        );

        ReflectionTestUtils.setField(
                pedido,
                "dataCriacao",
                dataCriacao
        );

        ReflectionTestUtils.setField(
                pedido,
                "valorBruto",
                valorFinal
        );

        ReflectionTestUtils.setField(
                pedido,
                "valorDesconto",
                BigDecimal.ZERO
        );

        ReflectionTestUtils.setField(
                pedido,
                "valorFinal",
                valorFinal
        );

        ReflectionTestUtils.setField(
                pedido,
                "cupom",
                cupom
        );

        return pedidoRepository.saveAndFlush(pedido);
    }

    private PedidoFiltroDTO filtroVazio() {
        return filtro(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private PedidoFiltroDTO filtro(
            StatusPedido status,
            Long idUsuario,
            String emailCliente,
            LocalDate dataInicio,
            LocalDate dataFim,
            BigDecimal valorMinimo,
            BigDecimal valorMaximo,
            String codigoCupom
    ) {
        return new PedidoFiltroDTO(
                status,
                idUsuario,
                emailCliente,
                dataInicio,
                dataFim,
                valorMinimo,
                valorMaximo,
                codigoCupom
        );
    }
}
