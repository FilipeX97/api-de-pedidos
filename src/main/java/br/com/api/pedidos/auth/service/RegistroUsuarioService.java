package br.com.api.pedidos.auth.service;

import br.com.api.pedidos.auth.dto.RegistraRequestDTO;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistroUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroUsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registrar(RegistraRequestDTO registraRequestDTO) {
        if (usuarioRepository.findByEmail(registraRequestDTO.email()).isPresent()) {
            throw new RuntimeException("Usuário já existe");
        }

        usuarioRepository.save(
                new Usuario(
                        registraRequestDTO.nome(),
                        registraRequestDTO.email(),
                        passwordEncoder.encode(registraRequestDTO.senha()),
                        Perfil.USER
                )
        );
    }

}
