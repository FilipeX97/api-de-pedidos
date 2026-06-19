package br.com.api.pedidos.order.promotion.engine;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.promotion.strategy.EstrategiaDesconto;
import br.com.api.pedidos.order.promotion.strategy.TipoGrupoDesconto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MotorPromocao {

    private final List<EstrategiaDesconto> estrategias;

    public MotorPromocao(
            List<EstrategiaDesconto> estrategias
    ) {
        this.estrategias = estrategias;
    }

    public void recalcular(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido inválido");
        }

        pedido.limparDescontos();

        BigDecimal descontoEstrutural = calcularGrupo(
                pedido,
                TipoGrupoDesconto.ESTRUTURAL
        );

        BigDecimal descontoPromocional = calcularGrupo(
                pedido,
                TipoGrupoDesconto.PROMOCIONAL
        );

        BigDecimal maior = descontoEstrutural.max(descontoPromocional);
        pedido.aplicarDesconto(maior);
    }

    private BigDecimal calcularGrupo(Pedido pedido, TipoGrupoDesconto grupo) {
        return estrategias.stream()
                .filter(estrategia -> estrategia.getGrupo() == grupo)
                .filter(estrategia -> estrategia.podeAplicar(pedido))
                .map(estrategia -> estrategia.calcularDesconto(pedido))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
