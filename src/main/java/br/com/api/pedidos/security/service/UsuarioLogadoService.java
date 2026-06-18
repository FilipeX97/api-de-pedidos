package br.com.api.pedidos.security.service;

import br.com.api.pedidos.security.userdetails.UsuarioSecurity;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioLogadoService {

    public Usuario getUsuarioLogado() {
        var auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException(
                    "Usuário não autenticado"
            );
        }

        if (!(auth.getPrincipal() instanceof UsuarioSecurity usuarioSecurity)) {
            throw new IllegalStateException(
                    "Usuário inválido"
            );
        }

        return usuarioSecurity.getUsuario();
    }

    public Long getIdUsuarioLogado() {
        return getUsuarioLogado().getId();
    }

    public boolean usuarioAutenticado() {
        var auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        return auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof UsuarioSecurity;
    }

}
