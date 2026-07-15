package br.com.api.pedidos.product.service;

import br.com.api.pedidos.product.dto.ProdutoRequestDTO;
import br.com.api.pedidos.product.dto.ProdutoResponseDTO;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.product.repository.ProdutoRepository;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.pagination.util.PaginacaoUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ProdutoService {

    private static final Map<String, String> CAMPOS_ORDENACAO =
            Map.ofEntries(
                    Map.entry("id", "id"),
                    Map.entry("nome", "nome"),
                    Map.entry("descricao", "descricao"),
                    Map.entry("preco", "preco"),
                    Map.entry("estoque", "estoque"),
                    Map.entry("ativo", "ativo")
            );

    private static final Sort ORDENACAO_PADRAO =
            Sort.by(
                    Sort.Order.asc("nome")
            );

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarProdutoPorId(Long id) {
        return produtoRepository
                .findById(id).map(ProdutoResponseDTO::from)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<ProdutoResponseDTO> listarProdutos(
            Pageable pageable
    ) {
        Pageable pageableValidado = PaginacaoUtils.normalizar(
                pageable,
                CAMPOS_ORDENACAO,
                ORDENACAO_PADRAO
        );

        var pagina = produtoRepository
                .findAll(pageableValidado)
                .map(ProdutoResponseDTO::from);

        return PaginaResponseDTO.from(pagina);
    }

    @Transactional
    public ProdutoResponseDTO criarProduto(ProdutoRequestDTO produtoRequestDTO) {
        Produto produto = new Produto(
                produtoRequestDTO.nome(),
                produtoRequestDTO.descricao(),
                produtoRequestDTO.preco(),
                produtoRequestDTO.estoque()
        );

        Produto produtoSalvo = produtoRepository.saveAndFlush(produto);
        return ProdutoResponseDTO.from(produtoSalvo);
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
