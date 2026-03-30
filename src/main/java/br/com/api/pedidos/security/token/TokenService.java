package br.com.api.pedidos.security.token;

import br.com.api.pedidos.user.entity.Usuario;

public interface TokenService {
    String gerarToken(Usuario usuario, String ip, String userAgent);
    boolean validarToken(String token);
}
