package br.com.api.pedidos.order.promotion.strategy;

import br.com.api.pedidos.order.entity.Pedido;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DescontoQuantidade implements EstrategiaDesconto {

    private static final int QUANTIDADE_MINIMA  = 10;
    private static final BigDecimal DESCONTO = BigDecimal.valueOf(0.10);

    @Override
    public TipoGrupoDesconto getGrupo() {
        return TipoGrupoDesconto.ESTRUTURAL;
    }

    @Override
    public boolean podeAplicar(Pedido pedido) {
        return pedido != null
                && pedido.getItens().stream()
                .anyMatch(item -> item.getQuantidade() >= QUANTIDADE_MINIMA);
    }

    @Override
    public BigDecimal calcularDesconto(Pedido pedido) {
        if (!podeAplicar(pedido)) {
            return BigDecimal.ZERO;
        }

        return pedido.getItens().stream()
                .filter(item -> item.getQuantidade() >= QUANTIDADE_MINIMA )
                .map(item -> item.getSubtotal().multiply(DESCONTO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
