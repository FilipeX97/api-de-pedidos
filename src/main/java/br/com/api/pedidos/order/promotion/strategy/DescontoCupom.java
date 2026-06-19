package br.com.api.pedidos.order.promotion.strategy;

import br.com.api.pedidos.order.entity.Pedido;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DescontoCupom implements EstrategiaDesconto {

    @Override
    public TipoGrupoDesconto getGrupo() {
        return TipoGrupoDesconto.PROMOCIONAL;
    }

    @Override
    public boolean podeAplicar(Pedido pedido) {
        return pedido != null
                && pedido.getCupom() != null;
    }

    @Override
    public BigDecimal calcularDesconto(Pedido pedido) {
        if (!podeAplicar(pedido)) {
            return BigDecimal.ZERO;
        }

        return pedido.getValorBruto()
                .multiply(pedido.getCupom().getPercentual());
    }

}
