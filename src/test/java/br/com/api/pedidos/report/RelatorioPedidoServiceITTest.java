package br.com.api.pedidos.report;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.order.state.StatusPedido;
import br.com.api.pedidos.report.dto.PeriodoRelatorioFiltroDTO;
import br.com.api.pedidos.report.service.RelatorioPedidoService;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=" +
                "jdbc:h2:mem:relatorio_pedido_it;" +
                "DB_CLOSE_DELAY=-1;" +
                "DB_CLOSE_ON_EXIT=FALSE"
})
class RelatorioPedidoServiceITTest {

    @Autowired
    private RelatorioPedidoService relatorioPedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveRetornarZerosQuandoNaoExistemPedidos() {
        var resultado = relatorioPedidoService.gerarResumo(
                new PeriodoRelatorioFiltroDTO(
                        null,
                        null
                )
        );

        assertEquals(0L, resultado.totalPedidos());
        assertEquals(0L, resultado.totalPedidosPagos());
        assertEquals(0L, resultado.totalPedidosCancelados());
        assertEquals(
                0L,
                resultado.totalPedidosAguardandoPagamento()
        );
        assertEquals(
                new BigDecimal("0.00"),
                resultado.valorTotalVendido()
        );
        assertEquals(
                new BigDecimal("0.00"),
                resultado.ticketMedio()
        );
    }

    @Test
    void deveConsiderarPagosEnviadosEEntreguesComoVenda() {
        Usuario usuario = criarUsuario();

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                new BigDecimal("100.00")
        );

        criarPedido(
                usuario,
                StatusPedido.ENVIADO,
                new BigDecimal("200.00")
        );

        criarPedido(
                usuario,
                StatusPedido.ENTREGUE,
                new BigDecimal("300.00")
        );

        criarPedido(
                usuario,
                StatusPedido.AGUARDANDO_PAGAMENTO,
                new BigDecimal("500.00")
        );

        var resultado = relatorioPedidoService.gerarResumo(
                periodoAtual()
        );

        assertEquals(4L, resultado.totalPedidos());
        assertEquals(3L, resultado.totalPedidosPagos());
        assertEquals(0L, resultado.totalPedidosCancelados());
        assertEquals(
                1L,
                resultado.totalPedidosAguardandoPagamento()
        );
        assertEquals(
                new BigDecimal("600.00"),
                resultado.valorTotalVendido()
        );
        assertEquals(
                new BigDecimal("200.00"),
                resultado.ticketMedio()
        );
    }

    @Test
    void naoDeveSomarPedidosCanceladosOuEstornados() {
        Usuario usuario = criarUsuario();

        criarPedido(
                usuario,
                StatusPedido.PAGO,
                new BigDecimal("100.00")
        );

        criarPedido(
                usuario,
                StatusPedido.CANCELADO,
                new BigDecimal("500.00")
        );

        criarPedido(
                usuario,
                StatusPedido.ESTORNADO,
                new BigDecimal("700.00")
        );

        var resultado = relatorioPedidoService.gerarResumo(
                periodoAtual()
        );

        assertEquals(3L, resultado.totalPedidos());
        assertEquals(1L, resultado.totalPedidosPagos());
        assertEquals(1L, resultado.totalPedidosCancelados());
        assertEquals(
                new BigDecimal("100.00"),
                resultado.valorTotalVendido()
        );
        assertEquals(
                new BigDecimal("100.00"),
                resultado.ticketMedio()
        );
    }

    private Usuario criarUsuario() {
        String identificador = UUID.randomUUID().toString();

        return usuarioRepository.saveAndFlush(
                new Usuario(
                        "Usuário Relatório",
                        identificador + "@teste.com",
                        "123456",
                        Perfil.USER
                )
        );
    }

    private Pedido criarPedido(
            Usuario usuario,
            StatusPedido status,
            BigDecimal valorFinal
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
                LocalDateTime.now()
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

        return pedidoRepository.saveAndFlush(pedido);
    }

    private PeriodoRelatorioFiltroDTO periodoAtual() {
        LocalDate hoje = LocalDate.now();

        return new PeriodoRelatorioFiltroDTO(
                hoje.minusDays(1),
                hoje.plusDays(1)
        );
    }
}
