package br.com.api.pedidos.shared.exception;

import br.com.api.pedidos.security.userdetails.UsuarioSecurity;
import br.com.api.pedidos.shared.response.RespostaApi;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaApi<Void>> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(RespostaApi.erro(exception.getMessage()));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<RespostaApi<Void>> tratarRegraNegocio(
            RegraNegocioException exception
    ) {
        log.warn(
                "Erro de regra de negocio. requestId={} mensagem={}",
                MDC.get("requestId"),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespostaApi.erro(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaApi<Map<String, String>>> tratarErroValidacao(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> erros = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (mensagemAtual, novaMensagem) -> mensagemAtual,
                        LinkedHashMap::new
                ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespostaApi.erro(erros, "Dados inválidos"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RespostaApi<Void>> tratarIllegalArgument(
            IllegalArgumentException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespostaApi.erro(exception.getMessage())
                );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RespostaApi<Void>> tratarIllegalState(
            IllegalStateException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(RespostaApi.erro(exception.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<RespostaApi<Void>> tratarSecurityException(
            SecurityException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(RespostaApi.erro(exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespostaApi<Void>> tratarAcessoNegado(
            AccessDeniedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(RespostaApi.erro(
                        "Acesso negado"
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespostaApi<Void>> tratarDataIntegrity(
            DataIntegrityViolationException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(RespostaApi.erro(
                        "Violação de integridade dos dados"
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespostaApi<Void>> tratarJsonInvalido(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespostaApi.erro(
                        "JSON inválido ou valor incompatível com o tipo esperado"
                ));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<RespostaApi<Void>> tratarHeaderObrigatorio(
            MissingRequestHeaderException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespostaApi.erro(
                        "Header obrigatório não enviado: "
                                + exception.getHeaderName()
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RespostaApi<Void>> tratarParametroObrigatorio(
            MissingServletRequestParameterException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespostaApi.erro(
                        "Parâmetro obrigatório não enviado: "
                                + exception.getParameterName()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespostaApi<Void>> tratarParametroInvalido(
            MethodArgumentTypeMismatchException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespostaApi.erro(
                        "Parâmetro inválido: " + exception.getName()
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RespostaApi<Void>> tratarMetodoNaoSuportado(
            HttpRequestMethodNotSupportedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(RespostaApi.erro("Método HTTP não suportado"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<RespostaApi<Void>> tratarRuntime(
            RuntimeException exception
    ) {
        log.error(
                "Erro interno inesperado. requestId={}",
                MDC.get("requestId"),
                exception
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RespostaApi.erro(
                        "Erro interno inesperado"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaApi<Void>> tratarErroInterno(
            Exception exception,
            HttpServletRequest request,
            Authentication authentication
    ) {
        String requestId = MDC.get("requestId");
        Long usuarioId = extrairUsuarioId(authentication);

        log.error(
                "Erro inesperado. requestId={} metodo={} endpoint={} usuarioId={}",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                usuarioId,
                exception
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        RespostaApi.erro(
                                "Erro interno no servidor"
                        )
                );
    }

    private Long extrairUsuarioId(
            Authentication authentication
    ) {
        if (authentication != null
                && authentication.getPrincipal()
                instanceof UsuarioSecurity usuarioSecurity) {
            return usuarioSecurity
                    .getUsuario()
                    .getId();
        }

        return null;
    }
}
