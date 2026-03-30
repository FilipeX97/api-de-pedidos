package br.com.api.pedidos.auth.service;

import br.com.api.pedidos.auth.dto.LoginRequestDTO;
import br.com.api.pedidos.auth.dto.LoginResponseDTO;
import br.com.api.pedidos.auth.dto.RefreshTokenRequestDTO;
import br.com.api.pedidos.auth.dto.RegistraRequestDTO;
import br.com.api.pedidos.auth.entity.RefreshToken;
import br.com.api.pedidos.auth.repository.RefreshTokenRepository;
import br.com.api.pedidos.security.jwt.JwtService;
import br.com.api.pedidos.security.login.LoginTentativaService;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginTentativaService loginTentativaService;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LoginTentativaService loginTentativaService) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginTentativaService = loginTentativaService;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        if(loginTentativaService.estaBloqueado(loginRequestDTO.email())) {
            throw new RuntimeException("Usuário bloqueado temporariamente");
        }

        var usuario = usuarioRepository.findByEmail(loginRequestDTO.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        boolean senhaValida = passwordEncoder
                .matches(loginRequestDTO.senha(), usuario.getSenha());

        if (!senhaValida)
            throw new RuntimeException("Senha incorreta");

        String accessToken = jwtService.gerarToken(usuario);
        String refreshToken = jwtService.gerarRefreshToken(usuario);

        refreshTokenRepository.save(
                new RefreshToken(
                        refreshToken,
                        new Date(System.currentTimeMillis() + 604800000),
                        usuario
                )
        );

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    public LoginResponseDTO refresh(RefreshTokenRequestDTO dto) {

        var tokenSalvo = refreshTokenRepository
                .findByToken(dto.refreshToken())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (tokenSalvo.isRevogado()) {
            throw new RuntimeException("Token revogado");
        }

        if (!jwtService.validarTokenRefresh(dto.refreshToken())) {
            throw new RuntimeException("Token expirado");
        }

        Usuario usuario = tokenSalvo.getUsuario();

        tokenSalvo.revogar();
        refreshTokenRepository.save(tokenSalvo);

        String novoAccessToken = jwtService.gerarToken(usuario);
        String novoRefreshToken = jwtService.gerarRefreshToken(usuario);

        refreshTokenRepository.save(
                new RefreshToken(
                        novoRefreshToken,
                        new Date(System.currentTimeMillis() + 604800000),
                        usuario
                )
        );

        return new LoginResponseDTO(novoAccessToken, novoRefreshToken);
    }

    public void registrarUsuario(RegistraRequestDTO  registraRequestDTO) {
        if (usuarioRepository.findByEmail(registraRequestDTO.email()).isPresent()) {
            throw new RuntimeException("Usuário já existe");
        }

        usuarioRepository.save(new Usuario(
                registraRequestDTO.nome(),
                registraRequestDTO.email(),
                passwordEncoder.encode(registraRequestDTO.senha()),
                Perfil.USER
        ));
    }

}
