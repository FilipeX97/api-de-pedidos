package br.com.api.pedidos.report.repository;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.state.StatusPedido;
import br.com.api.pedidos.report.projection.ResumoPedidosProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;

public interface RelatorioPedidoRepository extends Repository<Pedido, Long> {
    @Query("""
            SELECT
                COUNT(p) AS totalPedidos,

                COALESCE(
                    SUM(
                        CASE
                            WHEN p.status IN :statusVenda
                            THEN 1
                            ELSE 0
                        END
                    ),
                    0
                ) AS totalPedidosPagos,

                COALESCE(
                    SUM(
                        CASE
                            WHEN p.status = :statusCancelado
                            THEN 1
                            ELSE 0
                        END
                    ),
                    0
                ) AS totalPedidosCancelados,

                COALESCE(
                    SUM(
                        CASE
                            WHEN p.status = :statusAguardandoPagamento
                            THEN 1
                            ELSE 0
                        END
                    ),
                    0
                ) AS totalPedidosAguardandoPagamento,

                COALESCE(
                    SUM(
                        CASE
                            WHEN p.status IN :statusVenda
                            THEN p.valorFinal
                            ELSE 0
                        END
                    ),
                    0
                ) AS valorTotalVendido

            FROM Pedido p

            WHERE
                (
                    :dataInicio IS NULL
                    OR p.dataCriacao >= :dataInicio
                )
                AND
                (
                    :dataFimExclusiva IS NULL
                    OR p.dataCriacao < :dataFimExclusiva
                )
            """)
    ResumoPedidosProjection gerarResumo(
            @Param("dataInicio")
            LocalDateTime dataInicio,

            @Param("dataFimExclusiva")
            LocalDateTime dataFimExclusiva,

            @Param("statusVenda")
            Collection<StatusPedido> statusVenda,

            @Param("statusCancelado")
            StatusPedido statusCancelado,

            @Param("statusAguardandoPagamento")
            StatusPedido statusAguardandoPagamento
    );
}
