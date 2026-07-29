package br.com.api.pedidos.payment.webhook.service;

import br.com.api.pedidos.payment.adapter.fake.GatewayPagamentoFakeConsulta;
import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.facade.CheckoutFacade;
import br.com.api.pedidos.payment.webhook.document.entity
        .RegistroOperacionalWebhookPagamento;
import br.com.api.pedidos.payment.webhook.document.service
        .RegistroOperacionalWebhookPagamentoService;
import br.com.api.pedidos.payment.webhook.dto.FakePagamentoWebhookDTO;
import br.com.api.pedidos.payment.webhook.entity.WebhookPagamentoRecebido;
import br.com.api.pedidos.payment.webhook.service.result
        .ResultadoRegistroWebhook;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FakePagamentoWebhookService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    FakePagamentoWebhookService.class
            );

    private static final String TIPO_PAYMENT_UPDATED =
            "PAYMENT_UPDATED";

    private final ObjectMapper objectMapper;

    private final AssinaturaWebhookFakeService
            assinaturaWebhookFakeService;

    private final GatewayPagamentoFakeConsulta
            gatewayPagamentoFakeConsulta;

    private final CheckoutFacade checkoutFacade;

    private final WebhookPagamentoRecebidoService
            webhookPagamentoRecebidoService;

    private final RegistroOperacionalWebhookPagamentoService
            registroOperacionalWebhookPagamentoService;

    public FakePagamentoWebhookService(
            ObjectMapper objectMapper,
            AssinaturaWebhookFakeService assinaturaWebhookFakeService,
            GatewayPagamentoFakeConsulta gatewayPagamentoFakeConsulta,
            CheckoutFacade checkoutFacade,
            WebhookPagamentoRecebidoService webhookPagamentoRecebidoService,
            RegistroOperacionalWebhookPagamentoService registroOperacionalWebhookPagamentoService
    ) {
        this.objectMapper = objectMapper;
        this.assinaturaWebhookFakeService =
                assinaturaWebhookFakeService;
        this.gatewayPagamentoFakeConsulta =
                gatewayPagamentoFakeConsulta;
        this.checkoutFacade = checkoutFacade;
        this.webhookPagamentoRecebidoService =
                webhookPagamentoRecebidoService;
        this.registroOperacionalWebhookPagamentoService =
                registroOperacionalWebhookPagamentoService;
    }

    public PagamentoResponseDTO processarWebhook(
            String corpoOriginal,
            String assinatura
    ) {
        assinaturaWebhookFakeService.validarAssinatura(
                corpoOriginal,
                assinatura
        );

        FakePagamentoWebhookDTO dto = converter(corpoOriginal);

        validarWebhook(dto);

        log.info(
                "Webhook recebido. eventId={} status={}",
                dto.eventId(),
                dto.statusPagamento()
        );

        Optional<RegistroOperacionalWebhookPagamento>
                registroOperacional =
                registroOperacionalWebhookPagamentoService
                        .registrarRecebimento(
                                dto,
                                corpoOriginal
                        );

        ResultadoRegistroWebhook resultadoRegistro =
                registrarNoControleTransacional(
                        dto,
                        corpoOriginal,
                        registroOperacional
                );

        if (resultadoRegistro.duplicado()) {
            sinalizarDuplicidadeOperacional(
                    registroOperacional
            );
        }

        if (!resultadoRegistro.deveProcessar()) {
            return tratarWebhookDuplicadoIgnorado(
                    resultadoRegistro,
                    registroOperacional
            );
        }

        return processarEventoRecebido(
                resultadoRegistro.evento(),
                registroOperacional
        );
    }

    private ResultadoRegistroWebhook registrarNoControleTransacional(
            FakePagamentoWebhookDTO dto,
            String corpoOriginal,
            Optional<RegistroOperacionalWebhookPagamento>
                    registroOperacional
    ) {
        try {
            return webhookPagamentoRecebidoService
                    .registrarOuBuscarExistente(
                            dto,
                            corpoOriginal
                    );

        } catch (RuntimeException exception) {
            marcarRegistroOperacionalComoErro(
                    registroOperacional,
                    exception
            );

            throw exception;
        }
    }

    private PagamentoResponseDTO tratarWebhookDuplicadoIgnorado(
            ResultadoRegistroWebhook resultadoRegistro,
            Optional<RegistroOperacionalWebhookPagamento>
                    registroOperacional
    ) {
        WebhookPagamentoRecebido evento =
                resultadoRegistro.evento();

        try {
            PagamentoResponseDTO pagamento =
                    checkoutFacade
                            .buscarPagamentoPorCodigoTransacao(
                                    evento.getCodigoTransacao()
                            );

            marcarRegistroOperacionalComoDuplicado(
                    registroOperacional
            );

            log.info(
                    "Webhook duplicado ignorado. eventId={}",
                    evento.getEventId()
            );

            return pagamento;

        } catch (RuntimeException exception) {
            log.error(
                    "Erro ao recuperar pagamento de webhook "
                            + "duplicado. eventId={}",
                    evento.getEventId(),
                    exception
            );

            marcarRegistroOperacionalComoErro(
                    registroOperacional,
                    exception
            );

            throw exception;
        }
    }

    private PagamentoResponseDTO processarEventoRecebido(
            WebhookPagamentoRecebido evento,
            Optional<RegistroOperacionalWebhookPagamento>
                    registroOperacional
    ) {
        try {
            /*
             * Simula que o gateway externo alterou o status
             * da transação antes de a API consultar e processar
             * essa atualização.
             */
            gatewayPagamentoFakeConsulta
                    .simularAtualizacaoExterna(
                            evento.getCodigoTransacao(),
                            evento.getStatusRecebido()
                    );

            PagamentoResponseDTO pagamentoResponseDTO =
                    checkoutFacade.processarWebhookPagamento(
                            evento.getCodigoTransacao()
                    );

            webhookPagamentoRecebidoService
                    .marcarComoProcessado(evento);

            marcarRegistroOperacionalComoProcessado(
                    registroOperacional
            );

            log.info(
                    "Webhook processado. eventId={} status={}",
                    evento.getEventId(),
                    evento.getStatusRecebido()
            );

            return pagamentoResponseDTO;

        } catch (RuntimeException exception) {
            log.error(
                    "Erro ao processar webhook. eventId={}",
                    evento.getEventId(),
                    exception
            );

            registrarErroTransacional(
                    evento,
                    exception
            );

            marcarRegistroOperacionalComoErro(
                    registroOperacional,
                    exception
            );

            throw exception;
        }
    }

    private void registrarErroTransacional(
            WebhookPagamentoRecebido evento,
            RuntimeException exceptionProcessamento
    ) {
        try {
            webhookPagamentoRecebidoService
                    .marcarComoErro(
                            evento,
                            exceptionProcessamento
                    );

        } catch (RuntimeException exceptionPersistencia) {
            log.error(
                    "Falha ao registrar erro do webhook "
                            + "no PostgreSQL. eventId={}",
                    evento.getEventId(),
                    exceptionPersistencia
            );

            exceptionProcessamento.addSuppressed(
                    exceptionPersistencia
            );
        }
    }

    private void sinalizarDuplicidadeOperacional(
            Optional<RegistroOperacionalWebhookPagamento>
                    registroOperacional
    ) {
        registroOperacional.ifPresent(
                registroOperacionalWebhookPagamentoService
                        ::sinalizarDuplicidade
        );
    }

    private void marcarRegistroOperacionalComoProcessado(
            Optional<RegistroOperacionalWebhookPagamento>
                    registroOperacional
    ) {
        registroOperacional.ifPresent(
                registroOperacionalWebhookPagamentoService
                        ::marcarComoProcessado
        );
    }

    private void marcarRegistroOperacionalComoDuplicado(
            Optional<RegistroOperacionalWebhookPagamento>
                    registroOperacional
    ) {
        registroOperacional.ifPresent(
                registroOperacionalWebhookPagamentoService
                        ::marcarComoDuplicado
        );
    }

    private void marcarRegistroOperacionalComoErro(
            Optional<RegistroOperacionalWebhookPagamento>
                    registroOperacional,
            RuntimeException exception
    ) {
        registroOperacional.ifPresent(
                registro ->
                        registroOperacionalWebhookPagamentoService
                                .marcarComoErro(
                                        registro,
                                        exception
                                )
        );
    }

    private FakePagamentoWebhookDTO converter(
            String corpoOriginal
    ) {
        try {
            return objectMapper.readValue(
                    corpoOriginal,
                    FakePagamentoWebhookDTO.class
            );

        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Payload do webhook inválido",
                    exception
            );
        }
    }

    private void validarWebhook(
            FakePagamentoWebhookDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Payload do webhook é obrigatório"
            );
        }

        if (dto.eventId() == null
                || dto.eventId().isBlank()) {
            throw new IllegalArgumentException(
                    "EventId do webhook é obrigatório"
            );
        }

        if (!TIPO_PAYMENT_UPDATED.equals(dto.tipo())) {
            throw new IllegalArgumentException(
                    "Tipo de evento não suportado: "
                            + dto.tipo()
            );
        }

        if (dto.codigoTransacao() == null
                || dto.codigoTransacao().isBlank()) {
            throw new IllegalArgumentException(
                    "Código da transação é obrigatório"
            );
        }

        if (dto.statusPagamento() == null) {
            throw new IllegalArgumentException(
                    "Status do pagamento é obrigatório"
            );
        }

        if (dto.statusPagamento() != StatusPagamento.APROVADO
                && dto.statusPagamento()
                != StatusPagamento.RECUSADO
                && dto.statusPagamento()
                != StatusPagamento.PENDENTE) {
            throw new IllegalArgumentException(
                    "Status não permitido via webhook: "
                            + dto.statusPagamento()
            );
        }
    }
}