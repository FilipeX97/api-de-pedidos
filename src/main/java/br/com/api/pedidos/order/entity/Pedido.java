package br.com.api.pedidos.order.entity;

import br.com.api.pedidos.order.valueobject.ItemPedidoId;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.user.entity.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario usuario;

    private LocalDateTime dataCriacao;
    private BigDecimal valorTotal;

    @Version
    private Long versao;

    @OneToMany(
            mappedBy = "pedido",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }

        this.usuario = usuario;
        this.dataCriacao = LocalDateTime.now();
        this.valorTotal = BigDecimal.ZERO;
    }

    private void calcularValorTotal() {
        this.valorTotal = itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public List<ItemPedido> getItens() {
        return List.copyOf(itens);
    }

    public void alterarQuantidadeDoItem(
            ItemPedidoId itemId,
            Integer novaQuantidade) {

        validarItens();
        ItemPedido item = buscarItemPorId(itemId);
        item.alterarQuantidade(novaQuantidade);
        calcularValorTotal();
    }

    public void removerItem(ItemPedidoId itemId) {
        validarItens();
        ItemPedido item = buscarItemPorId(itemId);
        item.removerCompletamente();
        itens.remove(item);
        calcularValorTotal();
    }

    public void adicionarItem(Produto produto, Integer quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto é obrigatório");
        }

        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade inválida");
        }

        itens.stream().filter(i ->
                i.getProduto().equals(produto))
                .findFirst().ifPresentOrElse(i ->
                    i.adicionarQuantidade(quantidade),
                () -> {
                    ItemPedido item = new ItemPedido(
                            this,
                            produto,
                            quantidade);

                    this.itens.add(item);
                });

        calcularValorTotal();
    }

    private ItemPedido buscarItemPorId(ItemPedidoId itemId) {
        return itens.stream()
                .filter(i -> itemId.valor().equals(i.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado no pedido"));
    }

    private void validarItens() {
        if (itens.isEmpty()) {
            throw new IllegalStateException("Não há itens no pedido");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pedido pedido)) return false;
        if(id == null || pedido.id == null) return false;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
