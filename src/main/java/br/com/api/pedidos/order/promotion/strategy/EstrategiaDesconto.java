package br.com.api.pedidos.order.promotion.strategy;

import br.com.api.pedidos.order.entity.Pedido;

import java.math.BigDecimal;

public interface EstrategiaDesconto {
    TipoGrupoDesconto getGrupo();
    boolean podeAplicar(Pedido pedido);
    BigDecimal calcularDesconto(Pedido pedido);
}
