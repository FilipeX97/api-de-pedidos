package br.com.api.pedidos.payment.webhook.document.service;

import br.com.api.pedidos.payment.webhook.document.entity
        .RegistroOperacionalWebhookPagamento;
import br.com.api.pedidos.payment.webhook.document.repository
        .RegistroOperacionalWebhookPagamentoRepository;
import br.com.api.pedidos.payment.webhook.dto.FakePagamentoWebhookDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;

@Service
public class RegistroOperacionalWebhookPagamentoService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    RegistroOperacionalWebhookPagamentoService.class
            );

    private static final String ORIGEM_FAKE_GATEWAY =
            "FAKE_GATEWAY";

    private static final String CHAVE_MDC_REQUEST_ID =
            "requestId";

    private static final String MENSAGEM_ERRO_NAO_INFORMADA =
            "Erro não informado";

    private final RegistroOperacionalWebhookPagamentoRepository repository;

    public RegistroOperacionalWebhookPagamentoService(
            RegistroOperacionalWebhookPagamentoRepository repository
    ) {
        this.repository = repository;
    }

    public Optional<RegistroOperacionalWebhookPagamento>
    registrarRecebimento(
            FakePagamentoWebhookDTO fakePagamentoWebhookDTO,
            String payloadOriginal
    ) {
        String eventId = extrairEventId(
                fakePagamentoWebhookDTO
        );

        String codigoTransacao = extrairCodigoTransacao(
                fakePagamentoWebhookDTO
        );

        try {
            validarDTO(fakePagamentoWebhookDTO);

            RegistroOperacionalWebhookPagamento registro =
                    new RegistroOperacionalWebhookPagamento(
                            fakePagamentoWebhookDTO.eventId(),
                            fakePagamentoWebhookDTO.codigoTransacao(),
                            fakePagamentoWebhookDTO.statusPagamento(),
                            payloadOriginal,
                            obterRequestId(),
                            fakePagamentoWebhookDTO.tipo(),
                            ORIGEM_FAKE_GATEWAY
                    );

            RegistroOperacionalWebhookPagamento salvo =
                    repository.save(registro);

            log.debug(
                    "Registro operacional de webhook criado. "
                            + "id={} eventId={} codigoTransacao={}",
                    salvo.getId(),
                    salvo.getEventId(),
                    salvo.getCodigoTransacao()
            );

            return Optional.of(salvo);

        } catch (RuntimeException exception) {
            log.error(
                    "Falha ao criar registro operacional de webhook "
                            + "no MongoDB. eventId={} codigoTransacao={}. "
                            + "O processamento principal continuará.",
                    eventId,
                    codigoTransacao,
                    exception
            );

            return Optional.empty();
        }
    }

    public void sinalizarDuplicidade(
            RegistroOperacionalWebhookPagamento registro
    ) {
        atualizarComBestEffort(
                registro,
                RegistroOperacionalWebhookPagamento::sinalizarDuplicidade,
                "sinalizar duplicidade"
        );
    }

    public void marcarComoProcessado(
            RegistroOperacionalWebhookPagamento registro
    ) {
        atualizarComBestEffort(
                registro,
                RegistroOperacionalWebhookPagamento::marcarComoProcessado,
                "marcar como processado"
        );
    }

    public void marcarComoDuplicado(
            RegistroOperacionalWebhookPagamento registro
    ) {
        atualizarComBestEffort(
                registro,
                RegistroOperacionalWebhookPagamento::marcarComoDuplicado,
                "marcar como duplicado"
        );
    }

    public void marcarComoErro(
            RegistroOperacionalWebhookPagamento registro,
            Exception exception
    ) {
        String mensagemErro = extrairMensagemErro(exception);

        atualizarComBestEffort(
                registro,
                documento -> documento.marcarComoErro(
                        mensagemErro
                ),
                "marcar como erro"
        );
    }

    private void atualizarComBestEffort(
            RegistroOperacionalWebhookPagamento registro,
            Consumer<RegistroOperacionalWebhookPagamento> atualizacao,
            String operacao
    ) {
        if (registro == null) {
            log.warn(
                    "Não foi possível {} o registro operacional "
                            + "porque o documento não foi informado. "
                            + "O processamento principal continuará.",
                    operacao
            );

            return;
        }

        try {
            atualizacao.accept(registro);

            repository.save(registro);

            log.debug(
                    "Registro operacional de webhook atualizado. "
                            + "operacao={} id={} eventId={} "
                            + "status={} duplicado={}",
                    operacao,
                    registro.getId(),
                    registro.getEventId(),
                    registro.getStatusProcessamento(),
                    registro.isDuplicado()
            );

        } catch (RuntimeException exception) {
            log.error(
                    "Falha ao atualizar registro operacional de webhook "
                            + "no MongoDB. operacao={} eventId={} "
                            + "codigoTransacao={}. "
                            + "O processamento principal continuará.",
                    operacao,
                    registro.getEventId(),
                    registro.getCodigoTransacao(),
                    exception
            );
        }
    }

    private void validarDTO(
            FakePagamentoWebhookDTO fakePagamentoWebhookDTO
    ) {
        if (fakePagamentoWebhookDTO == null) {
            throw new IllegalArgumentException(
                    "DTO do webhook é obrigatório"
            );
        }
    }

    private String obterRequestId() {
        String requestId = MDC.get(CHAVE_MDC_REQUEST_ID);

        if (requestId == null || requestId.isBlank()) {
            return null;
        }

        return requestId;
    }

    private String extrairEventId(
            FakePagamentoWebhookDTO fakePagamentoWebhookDTO
    ) {
        if (fakePagamentoWebhookDTO == null) {
            return null;
        }

        return fakePagamentoWebhookDTO.eventId();
    }

    private String extrairCodigoTransacao(
            FakePagamentoWebhookDTO fakePagamentoWebhookDTO
    ) {
        if (fakePagamentoWebhookDTO == null) {
            return null;
        }

        return fakePagamentoWebhookDTO.codigoTransacao();
    }

    private String extrairMensagemErro(Exception exception) {
        if (exception == null) {
            return MENSAGEM_ERRO_NAO_INFORMADA;
        }

        String mensagemErro = exception.getMessage();

        if (mensagemErro == null || mensagemErro.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return mensagemErro;
    }
}