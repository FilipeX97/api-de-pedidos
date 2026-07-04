package br.com.api.pedidos.audit.entity;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.user.entity.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, updatable = false)
    private Pedido pedido;

    @Column(nullable = false, length = 80)
    private String recurso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAcao tipoAcao;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    protected Auditoria() {
    }

    public Auditoria(
            Usuario usuario,
            Pedido pedido,
            String recurso,
            TipoAcao tipoAcao,
            String descricao) {
        validarUsuario(usuario);
        validarPedido(pedido);
        validarRecurso(recurso);
        validarAcao(tipoAcao);
        validarDescricao(descricao);

        this.usuario = usuario;
        this.pedido = pedido;
        this.recurso = recurso;
        this.tipoAcao = tipoAcao;
        this.descricao = descricao;
        this.dataCriacao = LocalDateTime.now();
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário da auditoria é obrigatório");
        }
    }

    private void validarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido da auditoria é obrigatório");
        }
    }

    private void validarRecurso(String recurso) {
        if (recurso == null || recurso.isBlank()) {
            throw new IllegalArgumentException("Recurso da auditoria é obrigatório");
        }
    }

    private void validarAcao(TipoAcao tipoAcao) {
        if (tipoAcao == null) {
            throw new IllegalArgumentException("Ação da auditoria é obrigatória");
        }
    }

    private void validarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição da auditoria é obrigatória");
        }
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getIdUsuario() {
        return usuario.getId();
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Long getIdPedido() {
        return pedido.getId();
    }

    public String getRecurso() {
        return recurso;
    }

    public TipoAcao getAcao() {
        return tipoAcao;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Auditoria auditoria)) return false;
        return Objects.equals(id, auditoria.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
