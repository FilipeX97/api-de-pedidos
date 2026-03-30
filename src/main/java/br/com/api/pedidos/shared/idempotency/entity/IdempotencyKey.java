package br.com.api.pedidos.shared.idempotency.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String chave;
    @Lob
    private String respostaJson;
    private Date criadaEm;

    public IdempotencyKey() {
    }

    public IdempotencyKey(String chave, String respostaJson, Date criadaEm) {
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

    public Date getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(Date criadaEm) {
        this.criadaEm = criadaEm;
    }
}
