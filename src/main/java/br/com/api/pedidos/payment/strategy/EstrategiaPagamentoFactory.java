package br.com.api.pedidos.payment.strategy;

import br.com.api.pedidos.payment.entity.FormaPagamento;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class EstrategiaPagamentoFactory {

    private final Map<FormaPagamento, EstrategiaPagamento> estrategias;

    public EstrategiaPagamentoFactory(
            List<EstrategiaPagamento> estrategiasEncontradas) {
        this.estrategias = new EnumMap<>(FormaPagamento.class);

        for(EstrategiaPagamento estrategia : estrategiasEncontradas) {
            FormaPagamento formaPagamento = estrategia.getFormaPagamento();

            if(this.estrategias.containsKey(formaPagamento)) {
                throw new IllegalStateException(
                        "Já existe uma estratégia para a forma de pagamento "
                                + formaPagamento
                );
            }

            this.estrategias.put(formaPagamento, estrategia);
        }
    }

    public EstrategiaPagamento obter(FormaPagamento formaPagamento) {
        if (formaPagamento == null) {
            throw new IllegalArgumentException(
                    "Forma de pagamento é obrigatória"
            );
        }

        EstrategiaPagamento estrategia = estrategias.get(formaPagamento);

        if (estrategia == null) {
            throw new IllegalStateException(
                    "Forma de pagamento não suportada: " + formaPagamento
            );
        }

        return estrategia;
    }
}
