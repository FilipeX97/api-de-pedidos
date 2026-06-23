package br.com.api.pedidos.product.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer estoque;
    private boolean ativo;

    @Version
    private Long versao;

    public Produto() {
    }

    public Produto(String nome, String descricao, BigDecimal preco, Integer estoque) {
        validarNome(nome);
        validarPreco(preco);
        validarEstoque(estoque);

        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.ativo = true;
    }

    private void validarNome(String nome) {
        if(nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
    }

    private void validarPreco(BigDecimal preco) {
        if(preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero");
        }
    }

    private void validarEstoque(Integer estoque) {
        if(estoque == null || estoque < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo");
        }
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void alterarNome(String nome) {
        validarNome(nome);
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void alterarDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void alterarPreco(BigDecimal novoPreco) {
        validarPreco(novoPreco);
        this.preco = novoPreco;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void ajustarEstoque(Integer novoEstoque) {
        validarEstoque(novoEstoque);
        this.estoque = novoEstoque;
    }

    public void adicionarEstoque(Integer quantidade) {
        if(quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        this.estoque += quantidade;
    }

    public void removerEstoque(Integer quantidade) {
        if(quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if(this.estoque < quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente");
        }

        this.estoque -= quantidade;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Produto produto)) return false;
        return Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}