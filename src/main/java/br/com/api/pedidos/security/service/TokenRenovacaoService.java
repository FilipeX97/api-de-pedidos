package br.com.api.pedidos.security.service;

import br.com.api.pedidos.security.jwt.JwtService;
import br.com.api.pedidos.security.userdetails.UsuarioSecurity;
import br.com.api.pedidos.security.util.RequestUtils;
import br.com.api.pedidos.security.util.TokenUtils;
import br.com.api.pedidos.user.entity.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class TokenRenovacaoService {

    private final JwtService jwtService;

    public TokenRenovacaoService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void renovarSeNecessario(HttpServletRequest request,
                                    HttpServletResponse response,
                                    UsuarioSecurity usuarioSecurity) {
        String token = TokenUtils.extrairToken(request);

        if (jwtService.precisaRenovar(token)) {
            Usuario usuario = usuarioSecurity.getUsuario();
            String requestIp = RequestUtils.extrairIp(request);
            String userAgent = RequestUtils.extrairUserAgent(request);

            String novoToken = jwtService.gerarToken(
                    usuario,
                    requestIp,
                    userAgent
            );

            response.setHeader("X-New-Token", novoToken);
        }
    }

}
