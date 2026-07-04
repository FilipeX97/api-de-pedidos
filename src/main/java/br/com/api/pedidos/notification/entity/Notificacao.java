package br.com.api.pedidos.notification.entity;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.user.entity.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, updatable = false)
    private Pedido pedido;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacao tipo;

    @Column(nullable = false)
    private boolean lida;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    protected Notificacao() {}

    public Notificacao(
            Usuario usuario,
            Pedido pedido,
            String titulo,
            String mensagem,
            TipoNotificacao tipo) {
        validarUsuario(usuario);
        validarPedido(pedido);
        validarTitulo(titulo);
        validarMensagem(mensagem);
        validarTipo(tipo);

        this.usuario = usuario;
        this.pedido = pedido;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.tipo = tipo;
        this.lida = false;
        this.dataCriacao = LocalDateTime.now();
    }

    public void marcarComoLida() {
        this.lida = true;
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário da notificação é obrigatório");
        }
    }

    private void validarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido da notificação é obrigatório");
        }
    }

    private void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título da notificação é obrigatório");
        }
    }

    private void validarMensagem(String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("Mensagem da notificação é obrigatória");
        }
    }

    private void validarTipo(TipoNotificacao tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo da notificação é obrigatório");
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

    public String getTitulo() {
        return titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public boolean isLida() {
        return lida;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Notificacao that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
