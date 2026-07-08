package br.com.api.pedidos.order.entity;

import br.com.api.pedidos.coupon.entity.Cupom;
import br.com.api.pedidos.order.state.EstadoPedido;
import br.com.api.pedidos.order.state.StatusPedido;
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
    private BigDecimal valorBruto;
    private BigDecimal valorDesconto;
    private BigDecimal valorFinal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Cupom cupom;

    @Version
    private Long versao;

    @OneToMany(
            mappedBy = "pedido",
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE,
                    CascadeType.REMOVE
            },
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
        this.valorBruto = BigDecimal.ZERO;
        this.valorDesconto = BigDecimal.ZERO;
        this.valorFinal = BigDecimal.ZERO;
        this.status = StatusPedido.CRIADO;
    }

    private void recalcularValores() {
        this.valorBruto = itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.valorFinal = valorBruto.subtract(valorDesconto);
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

    public BigDecimal getValorBruto() {
        return valorBruto;
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public Cupom getCupom() {
        return cupom;
    }

    public boolean estaVazio() {
        return this.itens.isEmpty();
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void aplicarCupom(Cupom novoCupom) {
        if (novoCupom == null) {
            throw new IllegalArgumentException("Cupom inválido");
        }

        if (this.cupom != null && this.cupom.equals(novoCupom)) {
            return;
        }

        this.cupom = novoCupom;
    }

    public boolean possuiCupom() {
        return cupom != null;
    }

    public List<ItemPedido> getItens() {
        return List.copyOf(itens);
    }

    public void pagar(EstadoPedido estadoPedido) {
        this.status = estadoPedido.pagar(this);
    }

    public void aguardarPagamento(EstadoPedido estadoPedido) {
        this.status = estadoPedido.aguardarPagamento(this);
    }

    public void confirmarPagamento(EstadoPedido estadoPedido) {
        this.status = estadoPedido.confirmarPagamento(this);
    }

    public void enviar(EstadoPedido estadoPedido) {
        this.status = estadoPedido.enviar(this);
    }

    public void entregar(EstadoPedido estadoPedido) {
        this.status = estadoPedido.entregar(this);
    }

    public void cancelar(EstadoPedido estadoPedido) {
        var novoStatus = estadoPedido.cancelar(this);

        if (novoStatus == StatusPedido.CANCELADO) {
            devolverEstoqueDosItens();
        }

        this.status = novoStatus;
    }

    public void estornar(EstadoPedido estadoPedido) {
        this.status = estadoPedido.estornar(this);
        devolverEstoqueDosItens();
    }

    public void aplicarDesconto(BigDecimal desconto) {
        if (desconto == null) {
            desconto = BigDecimal.ZERO;
        }

        if (desconto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Desconto inválido");
        }

        this.valorDesconto = desconto;
        this.valorFinal = valorBruto.subtract(valorDesconto);
    }

    public void limparDescontos() {
        this.valorDesconto = BigDecimal.ZERO;
        this.valorFinal = valorBruto;
    }

    public void alterarQuantidadeDoItem(
            ItemPedidoId itemId,
            Integer novaQuantidade) {

        validarItens();
        ItemPedido item = buscarItemPorId(itemId);
        item.alterarQuantidade(novaQuantidade);
        recalcularValores();
    }

    public void removerItem(ItemPedidoId itemId) {
        validarItens();
        ItemPedido item = buscarItemPorId(itemId);
        item.removerCompletamente();
        itens.remove(item);
        recalcularValores();
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

        recalcularValores();
    }

    private ItemPedido buscarItemPorId(ItemPedidoId itemId) {
        return itens.stream()
                .filter(i -> itemId.valor().equals(i.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado no pedido"));
    }

    private void devolverEstoqueDosItens() {
        itens.forEach(ItemPedido::devolverEstoqueReservado);
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
