package br.com.api.pedidos.user.service;

import br.com.api.pedidos.user.dto.UsuarioRequestDTO;
import br.com.api.pedidos.user.dto.UsuarioResponseDTO;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO usuarioRequestDTO) {
        if(usuarioRepository.findByEmail(usuarioRequestDTO.email()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        var usuario = new Usuario(usuarioRequestDTO.nome(), usuarioRequestDTO.email(), usuarioRequestDTO.senha(), Perfil.USER);

        usuarioRepository.save(usuario);
        return UsuarioResponseDTO.from(usuario);
    }

    public UsuarioResponseDTO buscarUsuarioPorId(Long id){
        return usuarioRepository.findById(id)
                .map(UsuarioResponseDTO::from)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

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

        if(usuarioRequestDTO.nome() != null) {
            usuario.alterarNome(usuarioRequestDTO.nome());
        }

        if(usuarioRequestDTO.email() != null &&
                !usuarioRequestDTO.email().equalsIgnoreCase(usuario.getEmail())) {

            var existente = usuarioRepository.findByEmail(usuarioRequestDTO.email());

            if (existente.isPresent() && !existente.get().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("E-mail já cadastrado");
            }

            usuario.alterarEmail(usuarioRequestDTO.email());
        }

        if(usuarioRequestDTO.senha() != null) {
            usuario.alterarSenha(usuarioRequestDTO.senha());
        }

        usuarioRepository.save(usuario);
        return UsuarioResponseDTO.from(usuario);
    }

    public void removerUsuario(Long id) {
        usuarioRepository.findById(id)
                .ifPresentOrElse(
                        usuarioRepository::delete,
                        () -> {throw new RuntimeException("Usuário não encontrado");}
                );
    }
}
