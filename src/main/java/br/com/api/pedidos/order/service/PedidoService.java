package br.com.api.pedidos.order.service;

import br.com.api.pedidos.coupon.service.CupomService;
import br.com.api.pedidos.order.promotion.engine.MotorPromocao;
import br.com.api.pedidos.order.dto.AdicionarPedidoRequestDTO;
import br.com.api.pedidos.order.dto.AlterarQuantidadeItemRequestDTO;
import br.com.api.pedidos.order.dto.PedidoResponseDTO;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.order.valueobject.ItemPedidoId;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.product.repository.ProdutoRepository;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final CupomService cupomService;
    private final MotorPromocao motorPromocao;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository,
                         CupomService cupomService,
                         MotorPromocao motorPromocao) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.cupomService = cupomService;
        this.motorPromocao = motorPromocao;
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedidoPorId(Long idPedido, Usuario usuario) {
        return PedidoResponseDTO.from(buscarPedido(idPedido, usuario));
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
        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO adicionarItemPedido(
            Long idPedido,
            AdicionarPedidoRequestDTO adicionarPedidoRequestDTO,
            Usuario usuario
            ) {
        var pedido = buscarPedido(idPedido, usuario);
        var produto = buscarProduto(adicionarPedidoRequestDTO.idProduto());
        pedido.adicionarItem(produto, adicionarPedidoRequestDTO.quantidade());
        recalcularPedido(pedido);
        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO alterarQuantidadeItemPedido(
            Long idPedido,
            Long itemId,
            AlterarQuantidadeItemRequestDTO alterarQuantidadeItemRequestDTO,
            Usuario usuario) {
        var pedido = buscarPedido(idPedido, usuario);
        pedido.alterarQuantidadeDoItem(new ItemPedidoId(itemId), alterarQuantidadeItemRequestDTO.novaQuantidade());
        recalcularPedido(pedido);
        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO removerItemPedido(
            Long idPedido,
            Long itemId,
            Usuario usuario) {
        var pedido = buscarPedido(idPedido, usuario);
        pedido.removerItem(new ItemPedidoId(itemId));
        recalcularPedido(pedido);
        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO aplicarCupom(
            Long idPedido,
            Usuario usuario,
            String codigoCupom) {
        var pedido = buscarPedido(idPedido, usuario);
        var cupom = cupomService.buscarCupomValido(codigoCupom);
        pedido.aplicarCupom(cupom);
        recalcularPedido(pedido);
        return PedidoResponseDTO.from(pedido);
    }

    private void recalcularPedido(Pedido pedido) {
        motorPromocao.recalcular(pedido);
    }

    private Pedido buscarPedido(Long idPedido, Usuario usuario) {
        return pedidoRepository.findByIdAndUsuario(idPedido, usuario)
                .orElseThrow(() ->
                        new RuntimeException("Pedido não encontrado")
                );
    }

    private Produto buscarProduto(Long idProduto) {
        return produtoRepository.findById(idProduto)
                .orElseThrow(() ->
                        new RuntimeException("Produto não encontrado")
                );
    }

}
