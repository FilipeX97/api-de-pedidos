package br.com.api.pedidos.order.query.specification;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.query.dto.PedidoFiltroDTO;
import br.com.api.pedidos.order.state.StatusPedido;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

public final class PedidoSpecifications {
    private PedidoSpecifications() {}

    public static Specification<Pedido> comFiltros(
            PedidoFiltroDTO pedidoFiltroDTO
    ) {
        return comStatus(pedidoFiltroDTO.status())
                .and(comUsuario(pedidoFiltroDTO.idUsuario()))
                .and(comEmailCliente(pedidoFiltroDTO.emailCliente()))
                .and(criadoAPartirDe(pedidoFiltroDTO.dataInicio()))
                .and(criadoAte(pedidoFiltroDTO.dataFim()))
                .and(comValorMinimo(pedidoFiltroDTO.valorMinimo()))
                .and(comValorMaximo(pedidoFiltroDTO.valorMaximo()))
                .and(comCodigoCupom(pedidoFiltroDTO.codigoCupom()));
    }

    public static Specification<Pedido> comStatus(
            StatusPedido status
    ) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("status"),
                    status
            );
        };
    }

    public static Specification<Pedido> comUsuario(
            Long idUsuario
    ) {
        return (root, query, criteriaBuilder) -> {
            if (idUsuario == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("usuario").get("id"),
                    idUsuario
            );
        };
    }

    public static Specification<Pedido> comEmailCliente(
            String emailCliente
    ) {
        return (root, query, criteriaBuilder) -> {
            if (textoVazio(emailCliente)) {
                return criteriaBuilder.conjunction();
            }

            String emailNormalizado = emailCliente
                    .trim()
                    .toLowerCase(Locale.ROOT);

            return criteriaBuilder.equal(
                    criteriaBuilder.lower(
                            root.get("usuario").get("email")
                    ),
                    emailNormalizado
            );
        };
    }

    public static Specification<Pedido> criadoAPartirDe(
            LocalDate dataInicio
    ) {
        return (root, query, criteriaBuilder) -> {
            if (dataInicio == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("dataCriacao"),
                    dataInicio.atStartOfDay()
            );
        };
    }

    public static Specification<Pedido> criadoAte(
            LocalDate dataFim
    ) {
        return (root, query, criteriaBuilder) -> {
            if (dataFim == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThan(
                    root.get("dataCriacao"),
                    dataFim.plusDays(1).atStartOfDay()
            );
        };
    }

    public static Specification<Pedido> comValorMinimo(
            BigDecimal valorMinimo
    ) {
        return (root, query, criteriaBuilder) -> {
            if (valorMinimo == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("valorFinal"),
                    valorMinimo
            );
        };
    }

    public static Specification<Pedido> comValorMaximo(
            BigDecimal valorMaximo
    ) {
        return (root, query, criteriaBuilder) -> {
            if (valorMaximo == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("valorFinal"),
                    valorMaximo
            );
        };
    }

    public static Specification<Pedido> comCodigoCupom(
            String codigoCupom
    ) {
        return (root, query, criteriaBuilder) -> {
            if (textoVazio(codigoCupom)) {
                return criteriaBuilder.conjunction();
            }

            String codigoNormalizado = codigoCupom
                    .trim()
                    .toLowerCase(Locale.ROOT);

            var cupomJoin = root.join(
                    "cupom",
                    JoinType.INNER
            );

            return criteriaBuilder.equal(
                    criteriaBuilder.lower(
                            cupomJoin.get("codigo")
                    ),
                    codigoNormalizado
            );
        };
    }

    private static boolean textoVazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
