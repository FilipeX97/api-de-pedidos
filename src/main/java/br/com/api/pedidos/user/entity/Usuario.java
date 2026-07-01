package br.com.api.pedidos.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;

    @Column(unique = true)
    private String email;

    @JsonIgnore
    private String senha;
    private LocalDate dataCriacao;
    private Long senhaAlteradaEm;
    private boolean ativo;
    private boolean clienteVip;

    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    protected Usuario(){}

    public Usuario(String nome, String email, String senha, Perfil perfil) {
        validarNome(nome);
        validarEmail(email);
        validarSenha(senha);

        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.dataCriacao = LocalDate.now();
        this.senhaAlteradaEm = System.currentTimeMillis();
        this.ativo = true;
        this.clienteVip = false;
    }

    private void validarNome(String nome) {
        if(nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
    }

    private void validarEmail(String email) {
        if(email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório");
        }
        if(!email.contains("@")) {
            throw new IllegalArgumentException("E-mail inválido");
        }
    }

    private void validarSenha(String senha) {
        if(senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatório");
        }
    }

    public boolean isAdmin() {
        return Perfil.ADMIN.equals(this.perfil);
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

    public String getEmail() {
        return email;
    }

    public void alterarEmail(String email) {
        validarEmail(email);
        this.email = email;
    }

    @JsonIgnore
    public String getSenha() {
        return senha;
    }

    public void alterarSenha(String senha) {
        validarSenha(senha);
        this.senha = senha;
        this.senhaAlteradaEm = System.currentTimeMillis();
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public Long getSenhaAlteradaEm() {
        return senhaAlteradaEm;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public boolean isClienteVip() { return clienteVip; }

    public void desativarUsuario() {
        if (!this.ativo) {
            throw new IllegalStateException("Usuário já está desativado");
        }
        this.ativo = false;
    }

    public void ativarUsuario() {
        if (this.ativo) {
            throw new IllegalStateException("Usuário já está ativo");
        }
        this.ativo = true;
    }

    public void ativarClienteVip() {
        if (this.clienteVip) {
            throw new IllegalStateException("Usuário já é cliente VIP");
        }
        this.clienteVip = true;
    }

    public void desativarClienteVIP() {
        if (!this.clienteVip) {
            throw new IllegalStateException("Usuário não é cliente VIP");
        }
        clienteVip = false;
    }

    public void invalidarTokens() {
        this.senhaAlteradaEm = System.currentTimeMillis();
    }

    public void promoverParaAdmin() {
        this.perfil = Perfil.ADMIN;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Usuario usuario)) return false;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
