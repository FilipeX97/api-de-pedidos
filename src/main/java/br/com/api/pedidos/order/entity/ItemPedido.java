package br.com.api.pedidos.order.entity;

import br.com.api.pedidos.product.entity.Produto;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Pedido pedido;

    @ManyToOne
    private Produto produto;

    private BigDecimal precoUnitario;
    private Integer quantidade;

    protected ItemPedido() {
    }

    protected ItemPedido(Pedido pedido, Produto produto, Integer quantidade) {
        this.pedido = pedido;
        this.produto = produto;
        this.precoUnitario = produto.getPreco();

        validarQuantidade(quantidade);
        produto.removerEstoque(quantidade);
        this.quantidade = quantidade;
    }

    private void validarQuantidade(Integer quantidade) {
        if(quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
    }

    protected void adicionarQuantidade(Integer quantidade) {
        alterarQuantidade(this.quantidade + quantidade);
    }

    protected void alterarQuantidade(Integer novaQuantidade) {
        validarQuantidade(novaQuantidade);

        if (novaQuantidade.equals(this.quantidade)) {
            return;
        }

        int diferenca = novaQuantidade - this.quantidade;

        if (diferenca > 0) {
            produto.removerEstoque(diferenca);
        } else {
            produto.adicionarEstoque(Math.abs(diferenca));
        }

        this.quantidade = novaQuantidade;
    }

    protected void devolverEstoqueReservado() {
        if (this.quantidade != null && this.quantidade > 0) {
            produto.adicionarEstoque(this.quantidade);
        }
    }

    protected void removerCompletamente() {
        produto.adicionarEstoque(this.quantidade);
        this.quantidade = 0;
    }

    public Long getId() {
        return id;
    }

    public Produto getProduto() {
        return produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ItemPedido itemPedido)) return false;
        if (id == null || itemPedido.id == null) return false;
        return Objects.equals(id, itemPedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
