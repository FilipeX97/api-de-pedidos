package br.com.api.pedidos.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.Objects;

@Entity
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private Instant expiration;

    public BlacklistedToken() {
    }

    public BlacklistedToken(String token, Instant expiration) {
        this.token = token;
        this.expiration = expiration;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiration() {
        return expiration;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlacklistedToken that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
