package br.com.api.pedidos.user.service;

import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.pagination.util.PaginacaoUtils;
import br.com.api.pedidos.user.cache.UsuarioCacheService;
import br.com.api.pedidos.user.dto.UsuarioRequestDTO;
import br.com.api.pedidos.user.dto.UsuarioResponseDTO;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UsuarioService {

    private static final Map<String, String> CAMPOS_ORDENACAO =
            Map.ofEntries(
                    Map.entry("id", "id"),
                    Map.entry("nome", "nome"),
                    Map.entry("email", "email"),
                    Map.entry("perfil", "perfil"),
                    Map.entry("dataCriacao", "dataCriacao"),
                    Map.entry("ativo", "ativo"),
                    Map.entry("clienteVip", "clienteVip")
            );

    private static final Sort ORDENACAO_PADRAO =
            Sort.by(
                    Sort.Order.asc("nome")
            );

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioCacheService usuarioCacheService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            UsuarioCacheService usuarioCacheService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioCacheService = usuarioCacheService;
    }

    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO usuarioRequestDTO) {
        if (usuarioRepository.findByEmail(usuarioRequestDTO.email()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        var usuario = new Usuario(
                usuarioRequestDTO.nome(),
                usuarioRequestDTO.email(),
                passwordEncoder.encode(usuarioRequestDTO.senha()),
                Perfil.USER
        );

        usuarioRepository.save(usuario);
        return UsuarioResponseDTO.from(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioPorId(Long id) {
        return UsuarioResponseDTO.from(
                buscarUsuarioOuFalhar(id)
        );
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(UsuarioResponseDTO::from)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<UsuarioResponseDTO> listarUsuarios(
            Pageable pageable
    ) {
        Pageable pageableValidado =
                PaginacaoUtils.normalizar(
                        pageable,
                        CAMPOS_ORDENACAO,
                        ORDENACAO_PADRAO
                );

        var pagina = usuarioRepository
                .findAll(pageableValidado)
                .map(UsuarioResponseDTO::from);

        return PaginaResponseDTO.from(pagina);
    }

    public UsuarioResponseDTO atualizarUsuario(Long id, UsuarioRequestDTO usuarioRequestDTO) {
        Usuario usuario = buscarUsuarioOuFalhar(id);
        String emailAntigo = usuario.getEmail();
        boolean alterouCredenciais = false;

        if (usuarioRequestDTO.nome() != null) {
            usuario.alterarNome(usuarioRequestDTO.nome());
        }

        if (usuarioRequestDTO.email() != null &&
                !usuarioRequestDTO.email().equalsIgnoreCase(usuario.getEmail())) {

            var existente = usuarioRepository.findByEmail(usuarioRequestDTO.email());

            if (existente.isPresent() &&
                    !existente.get().getId().equals(usuario.getId())) {

                throw new IllegalArgumentException("E-mail já cadastrado");
            }

            usuario.alterarEmail(usuarioRequestDTO.email());
            alterouCredenciais = true;
        }

        if (usuarioRequestDTO.senha() != null) {
            usuario.alterarSenha(
                    passwordEncoder.encode(usuarioRequestDTO.senha())
            );
            alterouCredenciais = true;
        }

        usuarioRepository.save(usuario);

        if (alterouCredenciais) {
            usuarioCacheService.removerCacheUsuario(emailAntigo);
        }

        return UsuarioResponseDTO.from(usuario);
    }

    public void removerUsuario(Long id) {
        Usuario usuario = buscarUsuarioOuFalhar(id);
        usuario.invalidarTokens();
        usuarioRepository.delete(usuario);
        usuarioCacheService.removerCacheUsuario(usuario.getEmail());
    }

    public void desativarUsuario(Long id) {
        Usuario usuario = buscarUsuarioOuFalhar(id);
        usuario.desativarUsuario();
        usuario.invalidarTokens();
        usuarioRepository.save(usuario);
        usuarioCacheService.removerCacheUsuario(usuario.getEmail());
    }

    public void ativarUsuario(Long id) {
        Usuario usuario = buscarUsuarioOuFalhar(id);
        usuario.ativarUsuario();
        usuarioRepository.save(usuario);
    }

    private Usuario buscarUsuarioOuFalhar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário não encontrado"));
    }
}
