package br.com.api.pedidos.order.service;

import br.com.api.pedidos.coupon.service.CupomService;
import br.com.api.pedidos.order.event.*;
import br.com.api.pedidos.order.promotion.engine.MotorPromocao;
import br.com.api.pedidos.order.dto.AdicionarPedidoRequestDTO;
import br.com.api.pedidos.order.dto.AlterarQuantidadeItemRequestDTO;
import br.com.api.pedidos.order.dto.PedidoResponseDTO;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.order.state.EstadoPedido;
import br.com.api.pedidos.order.state.EstadoPedidoFactory;
import br.com.api.pedidos.order.state.StatusPedido;
import br.com.api.pedidos.order.valueobject.ItemPedidoId;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.product.repository.ProdutoRepository;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final CupomService cupomService;
    private final MotorPromocao motorPromocao;
    private final EstadoPedidoFactory estadoPedidoFactory;
    private final ApplicationEventPublisher eventPublisher;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository,
                         CupomService cupomService,
                         MotorPromocao motorPromocao,
                         EstadoPedidoFactory estadoPedidoFactory,
                         ApplicationEventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.cupomService = cupomService;
        this.motorPromocao = motorPromocao;
        this.estadoPedidoFactory = estadoPedidoFactory;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedidoPorId(Long idPedido, Usuario usuario) {
        return PedidoResponseDTO.from(buscarPedidoDoUsuario(idPedido, usuario));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarTodosPedidos(Usuario usuario) {
        return pedidoRepository.findAllByUsuario(usuario).stream()
                .map(PedidoResponseDTO::from)
                .toList();
    }

    @Transactional
    public PedidoResponseDTO criarPedido(Usuario usuario) {
        var pedido = pedidoRepository.save(new Pedido(usuario));

        eventPublisher.publishEvent(
                new PedidoCriadoEvent(
                        pedido.getId(),
                        usuario.getId(),
                        LocalDateTime.now()
                )
        );

        return PedidoResponseDTO.from(pedido);
    }

    public PedidoResponseDTO adicionarItemPedido(
            Long idPedido,
            AdicionarPedidoRequestDTO adicionarPedidoRequestDTO,
            Usuario usuario) {
        var pedido = buscarPedidoDoUsuario(idPedido, usuario);
        var estadoAtual = buscarEstadoAtual(pedido);
        validarPermissaoParaAlterarItens(estadoAtual, pedido);
        var produto = buscarProduto(adicionarPedidoRequestDTO.idProduto());
        pedido.adicionarItem(produto, adicionarPedidoRequestDTO.quantidade());
        recalcularPedido(pedido);
        pedidoRepository.save(pedido);
        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO alterarQuantidadeItemPedido(
            Long idPedido,
            Long itemId,
            AlterarQuantidadeItemRequestDTO alterarQuantidadeItemRequestDTO,
            Usuario usuario) {
        var pedido = buscarPedidoDoUsuario(idPedido, usuario);
        var estadoAtual = buscarEstadoAtual(pedido);
        validarPermissaoParaAlterarItens(estadoAtual, pedido);
        pedido.alterarQuantidadeDoItem(new ItemPedidoId(itemId), alterarQuantidadeItemRequestDTO.novaQuantidade());
        recalcularPedido(pedido);
        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO removerItemPedido(
            Long idPedido,
            Long itemId,
            Usuario usuario) {
        var pedido = buscarPedidoDoUsuario(idPedido, usuario);
        var estadoAtual = buscarEstadoAtual(pedido);
        validarPermissaoParaAlterarItens(estadoAtual, pedido);
        pedido.removerItem(new ItemPedidoId(itemId));
        recalcularPedido(pedido);
        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO aplicarCupom(
            Long idPedido,
            Usuario usuario,
            String codigoCupom) {
        var pedido = buscarPedidoDoUsuario(idPedido, usuario);
        var estadoAtual = buscarEstadoAtual(pedido);
        validarPermissaoParaAplicarCupom(estadoAtual, pedido);
        var cupom = cupomService.buscarCupomValido(codigoCupom);
        pedido.aplicarCupom(cupom);
        recalcularPedido(pedido);

        eventPublisher.publishEvent(
                new CupomAplicadoEvent(
                        pedido.getId(),
                        usuario.getId(),
                        cupom.getCodigo(),
                        pedido.getStatus(),
                        LocalDateTime.now()
                )
        );

        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO pagarPedido(Long idPedido, Usuario usuario) {
        var pedido = buscarPedidoDoUsuario(idPedido, usuario);
        recalcularPedido(pedido);
        var estadoAtual = buscarEstadoAtual(pedido);
        pedido.pagar(estadoAtual);

        if (pedido.possuiCupom()) {
            pedido.getCupom().registrarUso();
        }

        eventPublisher.publishEvent(
                new PedidoPagoEvent(
                        pedido.getId(),
                        usuario.getId(),
                        pedido.getValorFinal(),
                        LocalDateTime.now()
                )
        );

        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO enviarPedido(Long idPedido) {
        Pedido pedido = buscarPedidoParaAdministracao(idPedido);
        EstadoPedido estadoAtual = buscarEstadoAtual(pedido);

        pedido.enviar(estadoAtual);

        eventPublisher.publishEvent(
                new PedidoEnviadoEvent(
                        pedido.getId(),
                        pedido.getUsuario().getId(),
                        pedido.getStatus(),
                        LocalDateTime.now()
                )
        );

        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO entregarPedido(Long idPedido) {
        Pedido pedido = buscarPedidoParaAdministracao(idPedido);
        EstadoPedido estadoAtual = buscarEstadoAtual(pedido);

        pedido.entregar(estadoAtual);

        eventPublisher.publishEvent(
                new PedidoEntregueEvent(
                        pedido.getId(),
                        pedido.getUsuario().getId(),
                        pedido.getValorFinal(),
                        LocalDateTime.now()
                )
        );

        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO cancelarPedido(Long idPedido, Usuario usuario) {
        Pedido pedido = buscarPedidoDoUsuario(idPedido, usuario);
        EstadoPedido estadoAtual = buscarEstadoAtual(pedido);
        StatusPedido statusAnterior = pedido.getStatus();

        pedido.cancelar(estadoAtual);

        eventPublisher.publishEvent(
                new PedidoCanceladoEvent(
                        pedido.getId(),
                        pedido.getUsuario().getId(),
                        statusAnterior,
                        pedido.getStatus(),
                        LocalDateTime.now()
                )
        );

        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO estornarPedido(Long idPedido) {
        Pedido pedido = buscarPedidoParaAdministracao(idPedido);
        EstadoPedido estadoAtual = buscarEstadoAtual(pedido);

        /*
         * Futuramente aqui você chamaria um serviço de pagamento.
         *
         * Exemplo:
         * pagamentoService.estornar(pedido);
         *
         * Se o estorno der certo, aí sim muda o status.
         */

        pedido.estornar(estadoAtual);

        eventPublisher.publishEvent(
                new PedidoEstornadoEvent(
                        pedido.getId(),
                        pedido.getUsuario().getId(),
                        pedido.getValorFinal(),
                        LocalDateTime.now()
                )
        );

        return PedidoResponseDTO.from(pedido);
    }

    private void validarPermissaoParaAlterarItens(
            EstadoPedido estadoAtual,
            Pedido pedido) {

        if (!estadoAtual.permiteAlterarItens()) {
            throw new IllegalStateException(
                    "Pedido com status " + pedido.getStatus() + " não permite alterar itens."
            );
        }
    }

    private void validarPermissaoParaAplicarCupom(
            EstadoPedido estadoAtual,
            Pedido pedido) {

        if (!estadoAtual.permiteAplicarCupom()) {
            throw new IllegalStateException(
                    "Pedido com status " + pedido.getStatus() + " não permite aplicar cupom."
            );
        }
    }

    private EstadoPedido buscarEstadoAtual(Pedido pedido) {
        return estadoPedidoFactory.obter(pedido.getStatus());
    }

    private void recalcularPedido(Pedido pedido) {
        motorPromocao.recalcular(pedido);
    }

    private Pedido buscarPedidoDoUsuario(Long idPedido, Usuario usuario) {
        return pedidoRepository.findByIdAndUsuario(idPedido, usuario)
                .orElseThrow(() ->
                        new RuntimeException("Pedido não encontrado")
                );
    }

    private Pedido buscarPedidoParaAdministracao(Long idPedido) {
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    private Produto buscarProduto(Long idProduto) {
        return produtoRepository.findById(idProduto)
                .orElseThrow(() ->
                        new RuntimeException("Produto não encontrado")
                );
    }
}
