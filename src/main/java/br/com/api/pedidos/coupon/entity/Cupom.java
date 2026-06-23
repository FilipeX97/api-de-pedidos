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

    @Version
    private Long versao;

    protected Cupom() {
    }

    public Cupom(
            String codigo,
            BigDecimal percentual,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Integer limiteUso
    ) {
        validarCodigo(codigo);
        validarPercentual(percentual);
        validarPeriodo(dataInicio, dataFim);
        validarLimiteUso(limiteUso);

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

    public void ativar() {
        if (this.ativo) {
            throw new IllegalStateException("Cupom já está ativo");
        }

        this.ativo = true;
    }

    public void desativar() {
        if (!this.ativo) {
            throw new IllegalStateException("Cupom já está desativado");
        }

        this.ativo = false;
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

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Integer getLimiteUso() {
        return limiteUso;
    }

    public Integer getQuantidadeDeUso() {
        return quantidadeDeUso;
    }

    private void validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do cupom é obrigatório");
        }
    }

    private void validarPercentual(BigDecimal percentual) {
        if (percentual == null ||
                percentual.compareTo(BigDecimal.ZERO) <= 0 ||
                percentual.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Percentual do cupom deve ser entre 0 e 1");
        }
    }

    private void validarPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Período do cupom é obrigatório");
        }

        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data final não pode ser anterior à data inicial");
        }
    }

    private void validarLimiteUso(Integer limiteUso) {
        if (limiteUso == null || limiteUso <= 0) {
            throw new IllegalArgumentException("Limite de uso deve ser maior que zero");
        }
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
