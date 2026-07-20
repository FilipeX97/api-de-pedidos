package br.com.api.pedidos.payment.entity;

import br.com.api.pedidos.order.entity.Pedido;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPagamento statusPagamento;

    @Column(length = 100, unique = true)
    private String codigoTransacao;

    @Column(length = 1000)
    private String mensagemRetorno;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    protected Pagamento() {
    }

    public Pagamento(
            Pedido pedido,
            BigDecimal valor,
            FormaPagamento formaPagamento) {
        validarPedido(pedido);
        validarValor(valor);
        validarFormaPagamento(formaPagamento);

        this.pedido = pedido;
        this.valor = valor;
        this.formaPagamento = formaPagamento;
        this.statusPagamento = StatusPagamento.PENDENTE;
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void aprovar(String codigoTransacao, String mensagemRetorno) {
        this.statusPagamento = StatusPagamento.APROVADO;
        this.codigoTransacao = codigoTransacao;
        this.mensagemRetorno = mensagemRetorno;
        atualizarData();
    }

    public void recusar(String codigoTransacao, String mensagemRetorno) {
        this.statusPagamento = StatusPagamento.RECUSADO;
        this.codigoTransacao = codigoTransacao;
        this.mensagemRetorno = mensagemRetorno;
        atualizarData();
    }

    public void deixarPendente(String codigoTransacao, String mensagemRetorno) {
        this.statusPagamento = StatusPagamento.PENDENTE;
        this.codigoTransacao = codigoTransacao;
        this.mensagemRetorno = mensagemRetorno;
        atualizarData();
    }

    public void confirmarPagamentoPendente(
            String codigoTransacao,
            String mensagemRetorno) {
        if (this.statusPagamento != StatusPagamento.PENDENTE) {
            throw new IllegalStateException(
                    "Somente pagamento pendente pode ser confirmado"
            );
        }

        aprovar(codigoTransacao, mensagemRetorno);
    }

    public void cancelar(String mensagemRetorno) {
        this.statusPagamento = StatusPagamento.CANCELADO;
        this.mensagemRetorno = mensagemRetorno;
        atualizarData();
    }

    public void estornar(String codigoTransacao, String mensagemRetorno) {
        if (this.statusPagamento != StatusPagamento.APROVADO) {
            throw new IllegalStateException(
                    "Somente pagamento aprovado pode ser estornado"
            );
        }

        this.statusPagamento = StatusPagamento.ESTORNADO;
        this.codigoTransacao = codigoTransacao;
        this.mensagemRetorno = mensagemRetorno;
        atualizarData();
    }

    public boolean estaAprovado() {
        return statusPagamento == StatusPagamento.APROVADO;
    }

    public boolean estaPendente() {
        return statusPagamento == StatusPagamento.PENDENTE;
    }

    public boolean estaRecusado() {
        return statusPagamento == StatusPagamento.RECUSADO;
    }

    private void atualizarData() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    private void validarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido do pagamento é obrigatório");
        }
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero");
        }
    }

    private void validarFormaPagamento(FormaPagamento formaPagamento) {
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória");
        }
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Long getIdPedido() {
        return pedido.getId();
    }

    public BigDecimal getValor() {
        return valor;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public String getCodigoTransacao() {
        return codigoTransacao;
    }

    public String getMensagemRetorno() {
        return mensagemRetorno;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pagamento pagamento)) return false;
        return Objects.equals(id, pagamento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
