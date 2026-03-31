package br.com.api.pedidos.user.service;

import br.com.api.pedidos.cache.CacheNames;
import br.com.api.pedidos.security.userdetails.UsuarioSecurity;
import br.com.api.pedidos.user.cache.UsuarioCacheService;
import br.com.api.pedidos.user.dto.UsuarioRequestDTO;
import br.com.api.pedidos.user.dto.UsuarioResponseDTO;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioCacheService usuarioCacheService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          UsuarioCacheService usuarioCacheService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioCacheService = usuarioCacheService;
    }

    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO usuarioRequestDTO) {
        if(usuarioRepository.findByEmail(usuarioRequestDTO.email()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        var usuario = new Usuario(
                usuarioRequestDTO.nome(),
                usuarioRequestDTO.email(),
                passwordEncoder.encode(usuarioRequestDTO.senha()),
                Perfil.USER);

        usuarioRepository.save(usuario);
        return UsuarioResponseDTO.from(usuario);
    }

    public UsuarioResponseDTO buscarUsuarioPorId(Long id){
        return usuarioRepository.findById(id)
                .map(UsuarioResponseDTO::from)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @Cacheable(
            value = CacheNames.USUARIOS_DTO,
            key = "#email",
            unless = "#result == null",
            sync = true
    )
    public UsuarioResponseDTO buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(UsuarioResponseDTO::from)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponseDTO::from)
                .toList();
    }

    public UsuarioResponseDTO atualizarUsuario(Long id, UsuarioRequestDTO usuarioRequestDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String emailAntigo = null;

        if(usuarioRequestDTO.nome() != null) {
            usuario.alterarNome(usuarioRequestDTO.nome());
        }

        if(usuarioRequestDTO.email() != null &&
                !usuarioRequestDTO.email().equalsIgnoreCase(usuario.getEmail())) {

            var existente = usuarioRepository.findByEmail(usuarioRequestDTO.email());

            if (existente.isPresent() && !existente.get().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("E-mail já cadastrado");
            }

            emailAntigo = usuario.getEmail();
            usuario.alterarEmail(usuarioRequestDTO.email());
        }

        if(usuarioRequestDTO.senha() != null) {
            usuario.alterarSenha(
                    passwordEncoder.encode(usuarioRequestDTO.senha())
            );
        }

        usuarioRepository.save(usuario);

        if(emailAntigo != null) {
            usuarioCacheService.removerCacheUsuario(emailAntigo);
        }

        return UsuarioResponseDTO.from(usuario);
    }

    public void removerUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuarioCacheService.removerCacheUsuario(usuario.getEmail());
        usuarioRepository.delete(usuario);
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
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return new UsuarioSecurity(usuario);
    }
}
