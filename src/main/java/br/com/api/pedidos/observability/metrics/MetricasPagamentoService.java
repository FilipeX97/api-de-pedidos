package br.com.api.pedidos.observability.metrics;

import br.com.api.pedidos.payment.entity.FormaPagamento;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

@Service
public class MetricasPagamentoService {

    private static final String TAG_FORMA_PAGAMENTO =
            "forma_pagamento";

    private static final String METRICA_PAGAMENTOS_INICIADOS =
            "api.pedidos.pagamentos.iniciados";

    private static final String METRICA_PAGAMENTOS_APROVADOS =
            "api.pedidos.pagamentos.aprovados";

    private static final String METRICA_PAGAMENTOS_RECUSADOS =
            "api.pedidos.pagamentos.recusados";

    private static final String METRICA_PAGAMENTOS_PENDENTES =
            "api.pedidos.pagamentos.pendentes";

    private final Map<FormaPagamento, Counter> pagamentosIniciados;
    private final Map<FormaPagamento, Counter> pagamentosAprovados;
    private final Map<FormaPagamento, Counter> pagamentosRecusados;
    private final Map<FormaPagamento, Counter> pagamentosPendentes;

    public MetricasPagamentoService(MeterRegistry meterRegistry) {
        this.pagamentosIniciados = criarContadoresPorForma(
                meterRegistry,
                METRICA_PAGAMENTOS_INICIADOS,
                "Quantidade de pagamentos iniciados"
        );

        this.pagamentosAprovados = criarContadoresPorForma(
                meterRegistry,
                METRICA_PAGAMENTOS_APROVADOS,
                "Quantidade de pagamentos aprovados"
        );

        this.pagamentosRecusados = criarContadoresPorForma(
                meterRegistry,
                METRICA_PAGAMENTOS_RECUSADOS,
                "Quantidade de pagamentos recusados"
        );

        this.pagamentosPendentes = criarContadoresPorForma(
                meterRegistry,
                METRICA_PAGAMENTOS_PENDENTES,
                "Quantidade de pagamentos pendentes"
        );
    }

    public void registrarPagamentoIniciado(FormaPagamento formaPagamento) {
        obterContador(pagamentosIniciados, formaPagamento).increment();
    }

    public void registrarPagamentoAprovado(FormaPagamento formaPagamento) {
        obterContador(pagamentosAprovados, formaPagamento).increment();
    }

    public void registrarPagamentoRecusado(FormaPagamento formaPagamento) {
        obterContador(pagamentosRecusados, formaPagamento).increment();
    }

    public void registrarPagamentoPendente(FormaPagamento formaPagamento) {
        obterContador(pagamentosPendentes, formaPagamento).increment();
    }

    private Map<FormaPagamento, Counter> criarContadoresPorForma(
            MeterRegistry meterRegistry,
            String nomeMetrica,
            String descricao
    ) {
        Map<FormaPagamento, Counter> contadores =
                new EnumMap<>(FormaPagamento.class);

        for (FormaPagamento formaPagamento : FormaPagamento.values()) {
            Counter contador = Counter
                    .builder(nomeMetrica)
                    .description(descricao)
                    .tag(
                            TAG_FORMA_PAGAMENTO,
                            normalizarFormaPagamento(formaPagamento)
                    )
                    .register(meterRegistry);

            contadores.put(formaPagamento, contador);
        }

        return contadores;
    }

    private Counter obterContador(
            Map<FormaPagamento, Counter> contadores,
            FormaPagamento formaPagamento
    ) {
        if (formaPagamento == null) {
            throw new IllegalArgumentException(
                    "Forma de pagamento é obrigatória "
                            + "para registrar a métrica"
            );
        }

        return contadores.get(formaPagamento);
    }

    private String normalizarFormaPagamento(
            FormaPagamento formaPagamento
    ) {
        return formaPagamento
                .name()
                .toLowerCase(Locale.ROOT);
    }
}
