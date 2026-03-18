package br.com.api.pedidos.product.service;

import br.com.api.pedidos.product.dto.ProdutoRequestDTO;
import br.com.api.pedidos.product.dto.ProdutoResponseDTO;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.product.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public ProdutoResponseDTO buscarProdutoPorId(Long id) {
        return produtoRepository
                .findById(id).map(ProdutoResponseDTO::from)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
    }

    public List<ProdutoResponseDTO> listarProdutos() {
        return produtoRepository.findAll().stream()
                .map(ProdutoResponseDTO::from).toList();
    }

    public ProdutoResponseDTO criarProduto(ProdutoRequestDTO produtoRequestDTO) {
        var produto = new Produto(produtoRequestDTO.nome(), produtoRequestDTO.descricao(), produtoRequestDTO.preco(), produtoRequestDTO.estoque());
        produtoRepository.save(produto);
        return ProdutoResponseDTO.from(produto);
    }

    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoRequestDTO produtoRequestDTO) {
        var produto = produtoRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Produto não encontrado")
        );

        if(produtoRequestDTO.nome() != null) {
            produto.alterarNome(produtoRequestDTO.nome());
        }

        if(produtoRequestDTO.descricao() != null) {
            produto.alterarDescricao(produtoRequestDTO.descricao());
        }

        if(produtoRequestDTO.preco() != null) {
            produto.alterarPreco(produtoRequestDTO.preco());
        }

        if(produtoRequestDTO.estoque() != null) {
            produto.ajustarEstoque(produtoRequestDTO.estoque());
        }

        produtoRepository.save(produto);
        return ProdutoResponseDTO.from(produto);
    }

    public void removerProduto(Long id) {
        produtoRepository.findById(id).ifPresentOrElse(
                produtoRepository::delete,
                () -> {throw new IllegalArgumentException("Produto não encontrado");}
        );
    }

}
