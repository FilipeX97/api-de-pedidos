package br.com.api.pedidos.payment.service;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.payment.adapter.ResultadoPagamento;
import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.entity.FormaPagamento;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.repository.PagamentoRepository;
import br.com.api.pedidos.payment.strategy.EstrategiaPagamentoFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final EstrategiaPagamentoFactory estrategiaPagamentoFactory;

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            EstrategiaPagamentoFactory estrategiaPagamentoFactory) {
        this.pagamentoRepository = pagamentoRepository;
        this.estrategiaPagamentoFactory = estrategiaPagamentoFactory;
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
        var resultado = estrategia.processar(pagamento);
        aplicarResultado(pagamento, resultado);
        return pagamentoRepository.saveAndFlush(pagamento);
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
                        new RuntimeException("Pagamento não encontrado para este pedido")
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
