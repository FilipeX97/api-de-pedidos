package br.com.api.pedidos.auth.service;

import br.com.api.pedidos.auth.dto.LoginRequestDTO;
import br.com.api.pedidos.auth.dto.LoginResponseDTO;
import br.com.api.pedidos.auth.dto.RefreshTokenRequestDTO;
import br.com.api.pedidos.security.jwt.JwtService;
import br.com.api.pedidos.security.login.LoginTentativaService;
import br.com.api.pedidos.shared.exception.RecursoNaoEncontradoException;
import br.com.api.pedidos.shared.exception.RegraNegocioException;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    private static final Logger log =
            LoggerFactory.getLogger(AutenticacaoService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginTentativaService loginTentativaService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LoginTentativaService loginTentativaService,
            RefreshTokenService refreshTokenService,
            TokenBlacklistService tokenBlacklistService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginTentativaService = loginTentativaService;
        this.refreshTokenService = refreshTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public LoginResponseDTO login(
            LoginRequestDTO dto,
            String ip,
            String userAgent) {
        if (loginTentativaService.estaBloqueado(dto.email(), ip)) {
            log.warn(
                    "Falha de login. motivo=usuario_bloqueado ip={}",
                    ip
            );

            throw new RegraNegocioException(
                    "Usuário bloqueado temporariamente"
            );
        }

        var usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> {
                    log.warn(
                            "Falha de login. motivo=usuario_nao_encontrado ip={}",
                            ip
                    );

                    return new RecursoNaoEncontradoException(
                            "Usuário não encontrado"
                    );
                });

        if (!usuario.isAtivo()) {
            log.warn(
                    "Falha de login. motivo=usuario_desativado usuarioId={} ip={}",
                    usuario.getId(),
                    ip
            );

            throw new RegraNegocioException("Usuário desativado");
        }

        boolean senhaValida = passwordEncoder
                .matches(dto.senha(), usuario.getSenha());

        if (!senhaValida) {
            loginTentativaService.registrarFalha(dto.email(), ip);

            log.warn(
                    "Falha de login. motivo=senha_incorreta usuarioId={} ip={}",
                    usuario.getId(),
                    ip
            );

            throw new RegraNegocioException("Senha incorreta");
        }

        loginTentativaService.sucessoLogin(dto.email(), ip);
        String accessToken = jwtService.gerarToken(usuario, ip, userAgent);
        String refreshToken = refreshTokenService.criar(usuario);

        log.info(
                "Login realizado. usuarioId={} ip={}",
                usuario.getId(),
                ip
        );

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    public LoginResponseDTO refresh(
            RefreshTokenRequestDTO dto,
            String ip,
            String userAgent) {
        var tokenSalvo = refreshTokenService.buscar(dto.refreshToken());
        refreshTokenService.detectarUsoDeTokenRevogado(tokenSalvo);
        refreshTokenService.validarExpiracao(tokenSalvo);

        var usuario = tokenSalvo.getUsuario();
        refreshTokenService.revogar(tokenSalvo);

        String novoAccessToken = jwtService.gerarToken(usuario, ip, userAgent);
        String novoRefreshToken = refreshTokenService.criar(usuario);

        return new LoginResponseDTO(novoAccessToken, novoRefreshToken);
    }

    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authorizationHeader.substring(7);
        tokenBlacklistService.adicionarBlacklist(token);
        String email = jwtService.getEmail(token);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        refreshTokenService.revogarTodosTokensUsuario(usuario);
    }
}
