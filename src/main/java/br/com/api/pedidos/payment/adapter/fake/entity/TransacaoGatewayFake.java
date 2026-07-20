package br.com.api.pedidos.payment.adapter.fake.entity;

import br.com.api.pedidos.payment.entity.StatusPagamento;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_transacao_gateway_fake_codigo",
                columnNames = "codigo_transacao"
        )
)
public class TransacaoGatewayFake {

    private static final int TAMANHO_MAXIMO_CODIGO_TRANSACAO = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "codigo_transacao",
            nullable = false,
            length = TAMANHO_MAXIMO_CODIGO_TRANSACAO
    )
    private String codigoTransacao;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status_pagamento",
            nullable = false,
            length = 30
    )
    private StatusPagamento statusPagamento;

    @Column(
            name = "data_criacao",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataCriacao;

    @Column(
            name = "data_atualizacao",
            nullable = false
    )
    private LocalDateTime dataAtualizacao;

    protected TransacaoGatewayFake() {
    }

    public TransacaoGatewayFake(String codigoTransacao) {
        validarCodigoTransacao(codigoTransacao);

        LocalDateTime agora = LocalDateTime.now();

        this.codigoTransacao = codigoTransacao;
        this.statusPagamento = StatusPagamento.PENDENTE;
        this.dataCriacao = agora;
        this.dataAtualizacao = agora;
    }

    public void atualizarStatus(StatusPagamento novoStatus) {
        validarStatusPermitido(novoStatus);

        this.statusPagamento = novoStatus;
        this.dataAtualizacao = LocalDateTime.now();
    }

    private void validarCodigoTransacao(String codigoTransacao) {
        if (codigoTransacao == null || codigoTransacao.isBlank()) {
            throw new IllegalArgumentException(
                    "Código da transação é obrigatório"
            );
        }

        if (codigoTransacao.length()
                > TAMANHO_MAXIMO_CODIGO_TRANSACAO) {
            throw new IllegalArgumentException(
                    "Código da transação deve ter no máximo "
                            + TAMANHO_MAXIMO_CODIGO_TRANSACAO
                            + " caracteres"
            );
        }
    }

    private void validarStatusPermitido(
            StatusPagamento statusPagamento
    ) {
        if (statusPagamento == null) {
            throw new IllegalArgumentException(
                    "Status do pagamento é obrigatório"
            );
        }

        if (statusPagamento != StatusPagamento.PENDENTE
                && statusPagamento != StatusPagamento.APROVADO
                && statusPagamento != StatusPagamento.RECUSADO) {

            throw new IllegalArgumentException(
                    "Status não permitido no gateway fake: "
                            + statusPagamento
            );
        }
    }

    public Long getId() {
        return id;
    }

    public String getCodigoTransacao() {
        return codigoTransacao;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TransacaoGatewayFake that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
