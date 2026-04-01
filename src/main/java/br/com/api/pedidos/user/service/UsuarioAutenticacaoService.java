package br.com.api.pedidos.user.service;

import br.com.api.pedidos.cache.CacheNames;
import br.com.api.pedidos.security.userdetails.UsuarioSecurity;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAutenticacaoService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticacaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Cacheable(
            value = CacheNames.USUARIOS_AUTH,
            key = "#email",
            unless = "#result == null",
            sync = true
    )
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado"));

        return new UsuarioSecurity(usuario);
    }
}