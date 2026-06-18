package br.com.api.pedidos.order.service;

import br.com.api.pedidos.order.dto.AdicionarPedidoRequestDTO;
import br.com.api.pedidos.order.dto.AlterarQuantidadeItemRequestDTO;
import br.com.api.pedidos.order.dto.PedidoResponseDTO;
import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.order.valueobject.ItemPedidoId;
import br.com.api.pedidos.product.repository.ProdutoRepository;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedidoPorId(Long id, Usuario usuario) {
        return pedidoRepository.findByIdAndUsuario(id, usuario)
                .map(PedidoResponseDTO::from)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
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
        var pedido = pedidoRepository.findByIdAndUsuario(idPedido, usuario)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        var produto = produtoRepository.findById(adicionarPedidoRequestDTO.idProduto())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        pedido.adicionarItem(produto, adicionarPedidoRequestDTO.quantidade());
        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO alterarQuantidadeItemPedido(
            Long idPedido,
            Long itemId,
            AlterarQuantidadeItemRequestDTO alterarQuantidadeItemRequestDTO,
            Usuario usuario) {
        var pedido = pedidoRepository.findByIdAndUsuario(idPedido, usuario)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.alterarQuantidadeDoItem(new ItemPedidoId(itemId), alterarQuantidadeItemRequestDTO.novaQuantidade());
        return PedidoResponseDTO.from(pedido);
    }

    @Transactional
    public PedidoResponseDTO removerItemPedido(
            Long idPedido,
            Long itemId,
            Usuario usuario) {
        var pedido = pedidoRepository.findByIdAndUsuario(idPedido, usuario)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.removerItem(new ItemPedidoId(itemId));
        return PedidoResponseDTO.from(pedido);
    }

}
