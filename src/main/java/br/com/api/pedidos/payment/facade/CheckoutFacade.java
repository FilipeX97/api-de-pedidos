package br.com.api.pedidos.payment.facade;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.service.PedidoService;
import br.com.api.pedidos.order.state.StatusPedido;
import br.com.api.pedidos.payment.dto.PagamentoRequestDTO;
import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.entity.Pagamento;
import br.com.api.pedidos.payment.entity.StatusPagamento;
import br.com.api.pedidos.payment.service.PagamentoService;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CheckoutFacade {

    private final PedidoService pedidoService;
    private final PagamentoService pagamentoService;

    public CheckoutFacade(
            PedidoService pedidoService,
            PagamentoService pagamentoService) {
        this.pedidoService = pedidoService;
        this.pagamentoService = pagamentoService;
    }

    @Transactional
    public PagamentoResponseDTO processarPagamento(
            Long idPedido,
            Usuario usuario,
            PagamentoRequestDTO pagamentoRequestDTO) {
        validarRequest(pagamentoRequestDTO);

        var pedido = pedidoService.buscarPedidoDoUsuarioParaCheckout(
                idPedido,
                usuario
        );

        validarPedidoPodeIniciarPagamento(pedido);
        pedidoService.recalcularPedidoParaCheckout(pedido);

        var pagamento = pagamentoService.processarPagamento(
                pedido,
                pagamentoRequestDTO.formaPagamento()
        );

        aplicarResultadoPagamentoNoPedido(pagamento, pedido);
        return PagamentoResponseDTO.from(pagamento);
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listarPagamentosDoPedido(
            Long idPedido,
            Usuario usuario) {
        var pedido = pedidoService.buscarPedidoDoUsuarioParaCheckout(idPedido, usuario);
        return pagamentoService.listarPagamentosDoPedido(pedido.getId());
    }

    @Transactional
    public PagamentoResponseDTO processarWebhookPagamento(String codigoTransacao) {
        var pagamento = pagamentoService.processarConfirmacaoDoGateway(codigoTransacao);
        var pedido = pagamento.getPedido();

        if(pagamento.getStatusPagamento() == StatusPagamento.APROVADO) {
            if (pedido.getStatus() == StatusPedido.PAGO) {
                return PagamentoResponseDTO.from(pagamento);
            }

            if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
                throw new IllegalStateException(
                        "Somente pedido aguardando pagamento pode ser confirmado pelo webhook. Status atual: "
                                + pedido.getStatus()
                );
            }

            pedidoService.marcarPedidoComoPagoAposPagamento(pedido);
            return PagamentoResponseDTO.from(pagamento);
        }

        if (pagamento.getStatusPagamento() == StatusPagamento.RECUSADO) {
            return PagamentoResponseDTO.from(pagamento);
        }

        if (pagamento.getStatusPagamento() == StatusPagamento.PENDENTE) {
            return PagamentoResponseDTO.from(pagamento);
        }

        throw new IllegalStateException(
                "Status de pagamento não tratado no webhook: "
                        + pagamento.getStatusPagamento()
        );
    }

    @Transactional(readOnly = true)
    public PagamentoResponseDTO buscarPagamentoPorCodigoTransacao(
            String codigoTransacao) {
        Pagamento pagamento =
                pagamentoService.buscarPagamentoPorCodigoTransacao(
                        codigoTransacao
                );

        return PagamentoResponseDTO.from(pagamento);
    }

    private void aplicarResultadoPagamentoNoPedido(
            Pagamento pagamento,
            Pedido pedido) {
        if(pagamento.getStatusPagamento() == StatusPagamento.APROVADO) {
            pedidoService.marcarPedidoComoPagoAposPagamento(pedido);
            return;
        }

        if(pagamento.getStatusPagamento() == StatusPagamento.PENDENTE) {
            pedidoService.marcarPedidoComoAguardandoPagamento(pedido);
            return;
        }

        if(pagamento.getStatusPagamento() == StatusPagamento.RECUSADO) {
            return;
        }

        throw new IllegalStateException(
                "Status de pagamento não tratado no checkout: "
                        + pagamento.getStatusPagamento()
        );
    }

    private void validarRequest(PagamentoRequestDTO pagamentoRequestDTO) {
        if (pagamentoRequestDTO == null) {
            throw new IllegalArgumentException("Dados do pagamento são obrigatórios");
        }

        if (pagamentoRequestDTO.formaPagamento() == null) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória");
        }
    }

    private void validarPedidoPodeIniciarPagamento(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.CRIADO) {
            throw new IllegalStateException(
                    "Somente pedido com status CRIADO pode iniciar pagamento. Status atual: "
                            + pedido.getStatus()
            );
        }

        if (pedido.estaVazio()) {
            throw new IllegalStateException(
                    "Não é possível pagar um pedido sem itens"
            );
        }
    }

}
