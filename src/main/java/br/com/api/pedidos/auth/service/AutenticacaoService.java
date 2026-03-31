package br.com.api.pedidos.auth.service;

import br.com.api.pedidos.auth.dto.LoginRequestDTO;
import br.com.api.pedidos.auth.dto.LoginResponseDTO;
import br.com.api.pedidos.auth.dto.RefreshTokenRequestDTO;
import br.com.api.pedidos.security.jwt.JwtService;
import br.com.api.pedidos.security.login.LoginTentativaService;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginTentativaService loginTentativaService;
    private final RefreshTokenService refreshTokenService;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LoginTentativaService loginTentativaService,
            RefreshTokenService refreshTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginTentativaService = loginTentativaService;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponseDTO login(
            LoginRequestDTO dto,
            String ip,
            String userAgent) {

        if (loginTentativaService.estaBloqueado(dto.email(), ip)) {
            throw new RuntimeException("Usuário bloqueado temporariamente");
        }

        var usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!usuario.isAtivo()) {
            throw new RuntimeException("Usuário desativado");
        }

        boolean senhaValida = passwordEncoder
                .matches(dto.senha(), usuario.getSenha());

        if (!senhaValida) {
            loginTentativaService.registrarFalha(dto.email(), ip);
            throw new RuntimeException("Senha incorreta");
        }

        loginTentativaService.sucessoLogin(dto.email(), ip);
        String accessToken = jwtService.gerarToken(usuario, ip, userAgent);
        String refreshToken = refreshTokenService.criar(usuario);

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    public LoginResponseDTO refresh(
            RefreshTokenRequestDTO dto,
            String ip,
            String userAgent) {
        var tokenSalvo = refreshTokenService.buscar(dto.refreshToken());
        refreshTokenService.detectarUsoDeTokenRevogado(tokenSalvo);
        refreshTokenService.validarExpiracao(tokenSalvo);

        if (!jwtService.validarTokenRefresh(dto.refreshToken())) {
            throw new RuntimeException("Token expirado");
        }

        var usuario = tokenSalvo.getUsuario();
        refreshTokenService.revogar(tokenSalvo);

        String novoAccessToken = jwtService.gerarToken(usuario, ip, userAgent);
        String novoRefreshToken = refreshTokenService.criar(usuario);

        return new LoginResponseDTO(novoAccessToken, novoRefreshToken);
    }
}
