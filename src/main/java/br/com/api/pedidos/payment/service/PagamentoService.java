package br.com.api.pedidos.payment.service;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.payment.adapter.ResultadoPagamento;
import br.com.api.pedidos.payment.adapter.fake.GatewayPagamentoFakeConsulta;
import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.repository.PagamentoRepository;
import br.com.api.pedidos.payment.strategy.EstrategiaPagamentoFactory;
import br.com.api.pedidos.shared.exception.RecursoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PagamentoService {

    private static final Logger log =
            LoggerFactory.getLogger(PagamentoService.class);

    private final PagamentoRepository pagamentoRepository;
    private final EstrategiaPagamentoFactory estrategiaPagamentoFactory;
    private final GatewayPagamentoFakeConsulta gatewayPagamentoFakeConsulta;

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            EstrategiaPagamentoFactory estrategiaPagamentoFactory,
            GatewayPagamentoFakeConsulta gatewayPagamentoFakeConsulta) {
        this.pagamentoRepository = pagamentoRepository;
        this.estrategiaPagamentoFactory = estrategiaPagamentoFactory;
        this.gatewayPagamentoFakeConsulta = gatewayPagamentoFakeConsulta;
    }

    @Transactional
    public Pagamento processarPagamento(
            Pedido pedido,
            FormaPagamento formaPagamento) {
        validarPedido(pedido);
        validarFormaPagamento(formaPagamento);

        var pagamento = new Pagamento(
                pedido,
                pedido.getValorFinal(),
                formaPagamento
        );

        pagamentoRepository.saveAndFlush(pagamento);
        var estrategia = estrategiaPagamentoFactory.obter(formaPagamento);

        log.info(
                "Pagamento iniciado. pagamentoId={} pedidoId={} forma={} valor={}",
                pagamento.getId(),
                pedido.getId(),
                formaPagamento,
                pagamento.getValor()
        );

        var resultado = estrategia.processar(pagamento);
        aplicarResultado(pagamento, resultado);
        var pagamentoSalvo = pagamentoRepository.saveAndFlush(pagamento);

        log.info(
                "Pagamento processado. pagamentoId={} pedidoId={} status={}",
                pagamentoSalvo.getId(),
                pagamentoSalvo.getIdPedido(),
                pagamentoSalvo.getStatusPagamento()
        );

        return pagamentoSalvo;
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listarPagamentosDoPedido(Long idPedido) {
        return pagamentoRepository
                .findAllByPedidoIdOrderByDataCriacaoDesc(idPedido)
                .stream()
                .map(PagamentoResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Pagamento buscarPagamentoDoPedido(
            Long idPedido,
            Long idPagamento
    ) {
        return pagamentoRepository
                .findByIdAndPedidoId(idPagamento, idPedido)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Pagamento não encontrado para este pedido")
                );
    }

    @Transactional
    public Pagamento confirmarPagamentoPendente(
            Long idPedido,
            Long idPagamento) {
        var pagamento = buscarPagamentoDoPedido(idPedido, idPagamento);

        if(!pagamento.estaPendente()) {
            throw new IllegalStateException(
                    "Somente pagamento pendente pode ser confirmado"
            );
        }

        pagamento.confirmarPagamentoPendente(
                "CONFIRM-" + UUID.randomUUID(),
                "Pagamento pendente confirmado manualmente"
        );

        return pagamentoRepository.saveAndFlush(pagamento);
    }

    @Transactional
    public Pagamento processarConfirmacaoDoGateway(String codigoTransacao) {
        var pagamento = pagamentoRepository
                .findByCodigoTransacao(codigoTransacao)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Pagamento não encontrado pela transação")
                );

        StatusPagamento statusConfirmado =
                gatewayPagamentoFakeConsulta.consultarStatus(codigoTransacao);

        if (statusConfirmado == StatusPagamento.PENDENTE) {
            return pagamento;
        }

        if (statusConfirmado == StatusPagamento.APROVADO) {
            if (pagamento.estaAprovado()) {
                return pagamento;
            }

            if (!pagamento.estaPendente()) {
                throw new IllegalStateException(
                        "Somente pagamento pendente pode ser aprovado pelo gateway"
                );
            }

            pagamento.confirmarPagamentoPendente(
                    codigoTransacao,
                    "Pagamento confirmado pelo gateway fake via webhook"
            );

            return pagamentoRepository.saveAndFlush(pagamento);
        }

        if (statusConfirmado == StatusPagamento.RECUSADO) {
            if (pagamento.estaRecusado()) {
                return pagamento;
            }

            if (!pagamento.estaPendente()) {
                throw new IllegalStateException(
                        "Somente pagamento pendente pode ser recusado pelo gateway"
                );
            }

            pagamento.recusar(
                    codigoTransacao,
                    "Pagamento recusado pelo gateway fake via webhook"
            );

            return pagamentoRepository.saveAndFlush(pagamento);
        }

        throw new IllegalStateException(
                "Status de gateway não suportado: " + statusConfirmado
        );
    }

    @Transactional(readOnly = true)
    public Pagamento buscarPagamentoPorCodigoTransacao(
            String codigoTransacao) {
        if (codigoTransacao == null || codigoTransacao.isBlank()) {
            throw new IllegalArgumentException("Código da transação é obrigatório");
        }

        return pagamentoRepository
                .findByCodigoTransacao(codigoTransacao)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Pagamento não encontrado pela transação")
                );
    }

    private void aplicarResultado(
            Pagamento pagamento,
            ResultadoPagamento resultado) {
        if (resultado.statusPagamento() == StatusPagamento.APROVADO) {
            pagamento.aprovar(
                    resultado.codigoTransacao(),
                    resultado.mensagem()
            );
            return;
        }

        if (resultado.statusPagamento() == StatusPagamento.RECUSADO) {
            pagamento.recusar(
                    resultado.codigoTransacao(),
                    resultado.mensagem()
            );
            return;
        }

        if (resultado.statusPagamento() == StatusPagamento.PENDENTE) {
            pagamento.deixarPendente(
                    resultado.codigoTransacao(),
                    resultado.mensagem()
            );

            gatewayPagamentoFakeConsulta.registrarPagamentoPendente(
                    resultado.codigoTransacao()
            );

            return;
        }

        throw new IllegalStateException(
                "Status de pagamento não suportado: "
                        + resultado.statusPagamento()
        );
    }

    private void validarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido é obrigatório");
        }

        if (pedido.estaVazio()) {
            throw new IllegalStateException(
                    "Não é possível pagar um pedido sem itens"
            );
        }
    }

    private void validarFormaPagamento(FormaPagamento formaPagamento) {
        if (formaPagamento == null) {
            throw new IllegalArgumentException(
                    "Forma de pagamento é obrigatória"
            );
        }
    }
}
