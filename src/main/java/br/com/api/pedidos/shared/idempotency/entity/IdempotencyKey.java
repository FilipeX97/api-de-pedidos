package br.com.api.pedidos.shared.idempotency.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"chave", "endpoint", "metodoHttp", "usuarioId"}
                )
        }
)
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String chave;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private String usuarioId;

    @Column(nullable = false)
    private String metodoHttp;

    @Column(nullable = false)
    private String requestHash;

    @Column(
            name = "resposta_json",
            columnDefinition = "TEXT"
    )
    private String respostaJson;

    @Column(nullable = false)
    private Instant criadaEm;

    @Column(nullable = false)
    private Instant expiraEm;

    protected IdempotencyKey() {}

    public IdempotencyKey(
            String chave,
            String endpoint,
            String metodoHttp,
            String usuarioId,
            String requestHash,
            String respostaJson,
            Instant criadaEm,
            Instant expiraEm
    ) {
        this.chave = chave;
        this.endpoint = endpoint;
        this.metodoHttp = metodoHttp;
        this.usuarioId = usuarioId;
        this.requestHash = requestHash;
        this.respostaJson = respostaJson;
        this.criadaEm = criadaEm;
        this.expiraEm = expiraEm;
    }

    public Long getId() {
        return id;
    }

    public String getChave() {
        return chave;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getMetodoHttp() {
        return metodoHttp;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public String getRespostaJson() {
        return respostaJson;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }
}