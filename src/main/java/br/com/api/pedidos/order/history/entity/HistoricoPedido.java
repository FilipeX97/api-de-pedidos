package br.com.api.pedidos.order.history.entity;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.state.StatusPedido;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class HistoricoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, updatable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    protected HistoricoPedido() {}

    public HistoricoPedido(Pedido pedido, StatusPedido status, String descricao) {
        if(pedido == null)
            throw new IllegalArgumentException("Pedido é obrigatório");

        if(status == null)
            throw new IllegalArgumentException("Status é obrigatório");

        if(descricao == null)
            throw new IllegalArgumentException("Descrição é obrigatória");

        this.pedido = pedido;
        this.status = status;
        this.descricao = descricao;
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HistoricoPedido that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
