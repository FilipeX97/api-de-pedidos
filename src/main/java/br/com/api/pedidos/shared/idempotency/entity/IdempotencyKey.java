package br.com.api.pedidos.shared.idempotency.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Date;

@Entity
@Table(indexes = {
        @Index(name = "idx_idempotency_chave", columnList = "chave")
})
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String chave;
    @Lob
    private String respostaJson;
    private Instant criadaEm;

    public IdempotencyKey() {
    }

    public IdempotencyKey(String chave, String respostaJson, Instant criadaEm) {
        this.chave = chave;
        this.respostaJson = respostaJson;
        this.criadaEm = criadaEm;
    }

    public Long getId() {
        return id;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public String getRespostaJson() {
        return respostaJson;
    }

    public void setRespostaJson(String respostaJson) {
        this.respostaJson = respostaJson;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(Instant criadaEm) {
        this.criadaEm = criadaEm;
    }
}
