package br.com.api.pedidos.order.promotion.strategy;

import br.com.api.pedidos.order.entity.Pedido;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DescontoClienteVip implements EstrategiaDesconto {

    private static final BigDecimal DESCONTO = BigDecimal.valueOf(0.15);

    @Override
    public TipoGrupoDesconto getGrupo() {
        return TipoGrupoDesconto.ESTRUTURAL;
    }

    @Override
    public boolean podeAplicar(Pedido pedido) {
        return pedido != null
                && pedido.getUsuario() != null
                && pedido.getUsuario().isClienteVip();
    }

    @Override
    public BigDecimal calcularDesconto(Pedido pedido) {
        if (!podeAplicar(pedido)) {
            return BigDecimal.ZERO;
        }

        return pedido.getValorBruto().multiply(DESCONTO);
    }
}
