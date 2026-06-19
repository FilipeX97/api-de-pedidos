package br.com.api.pedidos.coupon.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private BigDecimal percentual;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private boolean ativo;
    private Integer limiteUso;
    private Integer quantidadeDeUso;

    protected Cupom() {
    }

    public Cupom(
            String codigo,
            BigDecimal percentual,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Integer limiteUso
    ) {
        this.codigo = codigo.toUpperCase();
        this.percentual = percentual;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.limiteUso = limiteUso;
        this.quantidadeDeUso = 0;
        this.ativo = true;
    }

    public boolean podeSerUtilizado() {
        LocalDateTime agora = LocalDateTime.now();

        return ativo
                && !agora.isBefore(dataInicio)
                && !agora.isAfter(dataFim)
                && quantidadeDeUso < limiteUso;
    }

    public void registrarUso() {
        if (!podeSerUtilizado()) {
            throw new IllegalStateException("Cupom indisponível");
        }

        quantidadeDeUso++;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public BigDecimal getPercentual() {
        return percentual;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cupom cupom)) return false;
        return Objects.equals(id, cupom.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
