package br.com.api.pedidos.shared.idempotency.service;

import br.com.api.pedidos.security.userdetails.UsuarioSecurity;
import br.com.api.pedidos.shared.idempotency.entity.IdempotencyKey;
import br.com.api.pedidos.shared.idempotency.repository.IdempotencyRepository;
import br.com.api.pedidos.shared.idempotency.util.GeradorHashRequest;
import br.com.api.pedidos.shared.response.RespostaApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class IdempotencyService {

    private static final long EXPIRACAO_HORAS = 24;

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;
    private final GeradorHashRequest geradorHashRequest;

    public IdempotencyService(
            IdempotencyRepository idempotencyRepository,
            ObjectMapper objectMapper,
            GeradorHashRequest geradorHashRequest
    ) {
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
        this.geradorHashRequest = geradorHashRequest;
    }

    public <T> RespostaApi<T> executar(
            String chave,
            HttpServletRequest request,
            Object requestBody,
            Class<T> responseType,
            Supplier<T> action,
            String mensagemSucesso
    ) {
        String endpoint = request.getRequestURI()
                .replaceAll("/$", "")
                .toLowerCase();
        String metodo = request.getMethod();

        var respostaCache = buscarResposta(
                chave,
                endpoint,
                metodo,
                requestBody
        );

        if (respostaCache.isPresent()) {
            try {
                T resposta = objectMapper.readValue(
                        respostaCache.get(),
                        responseType
                );

                return RespostaApi.sucesso(
                        resposta,
                        "Requisição já processada anteriormente (idempotência)"
                );

            } catch (Exception e) {
                throw new RuntimeException("Erro ao recuperar resposta idempotente", e);
            }
        }

        T resultado = action.get();

        salvarResposta(
                chave,
                endpoint,
                metodo,
                requestBody,
                resultado
        );

        return RespostaApi.sucesso(resultado, mensagemSucesso);
    }

    public Optional<String> buscarResposta(
            String chave,
            String endpoint,
            String metodo,
            Object request
    ) {
        String usuarioId = getUsuarioId();

        Optional<IdempotencyKey> registro =
                idempotencyRepository
                        .findByChaveAndEndpointAndMetodoHttpAndUsuarioId(
                                chave,
                                endpoint,
                                metodo,
                                usuarioId
                        );

        if (registro.isEmpty()) {
            return Optional.empty();
        }

        IdempotencyKey key = registro.get();

        if (key.getExpiraEm() != null &&
                key.getExpiraEm().isBefore(Instant.now())) {
            return Optional.empty();
        }

        String hash = geradorHashRequest.gerarHash(request);

        if (!hash.equals(key.getRequestHash())) {
            throw new IllegalStateException(
                    "Idempotency-Key reutilizada com payload diferente"
            );
        }

        return Optional.ofNullable(key.getRespostaJson());
    }

    public void salvarResposta(
            String chave,
            String endpoint,
            String metodo,
            Object request,
            Object resposta
    ) {
        String usuarioId = getUsuarioId();
        String hash = geradorHashRequest.gerarHash(request);

        try {
            String json = objectMapper.writeValueAsString(resposta);
            Instant agora = Instant.now();

            IdempotencyKey key = new IdempotencyKey(
                    chave,
                    endpoint,
                    metodo,
                    usuarioId,
                    hash,
                    json,
                    agora,
                    agora.plus(EXPIRACAO_HORAS, ChronoUnit.HOURS)
            );

            idempotencyRepository.saveAndFlush(key);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {

            Optional<IdempotencyKey> existente =
                    idempotencyRepository
                            .findByChaveAndEndpointAndMetodoHttpAndUsuarioId(
                                    chave,
                                    endpoint,
                                    metodo,
                                    usuarioId
                            );;

            if (existente.isPresent()) {
                IdempotencyKey keyExistente = existente.get();

                if (!hash.equals(keyExistente.getRequestHash())) {
                    throw new IllegalStateException(
                            "Idempotency-Key reutilizada com payload diferente"
                    );
                }

                return;
            }

            throw new IllegalStateException(
                    "Erro de concorrência ao salvar idempotency key"
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar idempotency key", e);
        }
    }

    private String getUsuarioId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UsuarioSecurity usuarioSecurity)) {
            throw new IllegalStateException("Usuário não autenticado");
        }

        return usuarioSecurity.getUsuario().getId().toString();
    }
}