package br.com.api.pedidos.user.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private String senha;
    private LocalDate dataCriacao;

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

    public String getSenha() {
        return senha;
    }

    public void alterarSenha(String senha) {
        validarSenha(senha);
        this.senha = senha;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public Perfil getPerfil() {
        return perfil;
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
