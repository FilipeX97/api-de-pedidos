package br.com.api.pedidos.order.state;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public final class EstadoPedidoFactory {

    private final Map<StatusPedido, EstadoPedido> estados;

    public EstadoPedidoFactory(List<EstadoPedido> estadosEncontrados) {
        this.estados = new EnumMap<>(StatusPedido.class);

        for (EstadoPedido estado :  estadosEncontrados) {
            StatusPedido status = estado.getStatus();

            if (this.estados.containsKey(status)) {
                throw new IllegalStateException("Já existe um estado para o status " + status);
            }

            this.estados.put(status, estado);
        }
    }

    public EstadoPedido obter(StatusPedido status) {
        if (status == null) {
            throw new IllegalStateException("Status do pedido não informado");
        }

        EstadoPedido estado = estados.get(status);

        if (estado == null) {
            throw new IllegalStateException("Status do pedido não suportado: " + status);
        }

        return estado;
    }

}
