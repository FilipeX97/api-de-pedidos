package br.com.api.pedidos.report.service;

import br.com.api.pedidos.order.state.StatusPedido;
import br.com.api.pedidos.report.dto.PeriodoRelatorioFiltroDTO;
import br.com.api.pedidos.report.dto.ResumoPedidosResponseDTO;
import br.com.api.pedidos.report.projection.ResumoPedidosProjection;
import br.com.api.pedidos.report.repository.RelatorioPedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

@Service
public class RelatorioPedidoService {

    private static final Set<StatusPedido> STATUS_COM_VENDA =
            EnumSet.of(
                    StatusPedido.PAGO,
                    StatusPedido.ENVIADO,
                    StatusPedido.ENTREGUE
            );

    private final RelatorioPedidoRepository relatorioPedidoRepository;

    public RelatorioPedidoService(
            RelatorioPedidoRepository relatorioPedidoRepository
    ) {
        this.relatorioPedidoRepository = relatorioPedidoRepository;
    }

    @Transactional(readOnly = true)
    public ResumoPedidosResponseDTO gerarResumo(PeriodoRelatorioFiltroDTO filtro) {
        validarFiltro(filtro);
        LocalDateTime dataInicio = converterDataInicio(filtro);
        LocalDateTime dataFimExclusiva = converterDataFim(filtro);

        ResumoPedidosProjection projection =
                relatorioPedidoRepository.gerarResumo(
                        dataInicio,
                        dataFimExclusiva,
                        STATUS_COM_VENDA,
                        StatusPedido.CANCELADO,
                        StatusPedido.AGUARDANDO_PAGAMENTO
                );

        Long totalPedidos = valorOuZero(projection.getTotalPedidos());
        Long totalPedidosPagos = valorOuZero(projection.getTotalPedidosPagos());

        Long totalPedidosCancelados =
                valorOuZero(
                        projection.getTotalPedidosCancelados()
                );

        Long totalAguardandoPagamento =
                valorOuZero(
                        projection
                                .getTotalPedidosAguardandoPagamento()
                );

        BigDecimal valorTotalVendido =
                valorOuZero(
                        projection.getValorTotalVendido()
                );

        BigDecimal ticketMedio =
                calcularTicketMedio(
                        valorTotalVendido,
                        totalPedidosPagos
                );

        return new ResumoPedidosResponseDTO(
                totalPedidos,
                totalPedidosPagos,
                totalPedidosCancelados,
                totalAguardandoPagamento,
                valorTotalVendido,
                ticketMedio
        );
    }

    private void validarFiltro(PeriodoRelatorioFiltroDTO filtro) {
        if (filtro == null) {
            throw new IllegalArgumentException("Filtro do relatório é obrigatório");
        }

        if (filtro.dataInicio() == null
                || filtro.dataFim() == null) {
            return;
        }

        if (filtro.dataInicio().isAfter(filtro.dataFim())) {
            throw new IllegalArgumentException(
                    "Data inicial não pode ser posterior à data final"
            );
        }
    }

    private LocalDateTime converterDataInicio(PeriodoRelatorioFiltroDTO filtro) {
        if (filtro.dataInicio() == null) {
            return null;
        }

        return filtro.dataInicio().atStartOfDay();
    }

    private LocalDateTime converterDataFim(PeriodoRelatorioFiltroDTO filtro) {
        if (filtro.dataFim() == null) {
            return null;
        }

        return filtro.dataFim()
                .plusDays(1)
                .atStartOfDay();
    }

    private long valorOuZero(Long valor) {
        return valor == null ? 0L : valor;
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        if (valor == null) {
            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        return valor.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal calcularTicketMedio(
            BigDecimal valorTotalVendido,
            long totalPedidosPagos
    ) {
        if (totalPedidosPagos == 0) {
            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        return valorTotalVendido.divide(
                BigDecimal.valueOf(totalPedidosPagos),
                2,
                RoundingMode.HALF_UP
        );
    }
}
