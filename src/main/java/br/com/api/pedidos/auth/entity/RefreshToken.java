package br.com.api.pedidos.auth.entity;

import br.com.api.pedidos.user.entity.Usuario;
import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private Date expiration;
    private boolean revogado;

    @ManyToOne
    private Usuario usuario;

    public RefreshToken() {
    }

    public RefreshToken(String token, Date expiration, Usuario  usuario) {
        this.token = token;
        this.expiration = expiration;
        this.usuario = usuario;
        revogado = false;
    }

    public boolean estaExpirado() {
        return expiration.before(new Date());
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Date getExpiration() {
        return expiration;
    }

    public boolean isRevogado() {
        return revogado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void revogar() {
        this.revogado = true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RefreshToken that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
