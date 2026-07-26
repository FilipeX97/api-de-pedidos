package br.com.api.pedidos.product.service;

import br.com.api.pedidos.product.dto.ProdutoAtualizacaoRequest;
import br.com.api.pedidos.product.dto.ProdutoCriacaoRequestDTO;
import br.com.api.pedidos.product.dto.ProdutoResponseDTO;
import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.product.repository.ProdutoRepository;
import br.com.api.pedidos.shared.exception.RecursoNaoEncontradoException;
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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
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
    public ProdutoResponseDTO criarProduto(ProdutoCriacaoRequestDTO produtoCriacaoRequestDTO) {
        Produto produto = new Produto(
                produtoCriacaoRequestDTO.nome(),
                produtoCriacaoRequestDTO.descricao(),
                produtoCriacaoRequestDTO.preco(),
                produtoCriacaoRequestDTO.estoque()
        );

        Produto produtoSalvo = produtoRepository.saveAndFlush(produto);
        return ProdutoResponseDTO.from(produtoSalvo);
    }

    @Transactional
    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoAtualizacaoRequest produtoAtualizacaoRequest) {
        var produto = produtoRepository.findById(id).orElseThrow(
                () -> new RecursoNaoEncontradoException("Produto não encontrado")
        );

        if(produtoAtualizacaoRequest.nome() != null) {
            produto.alterarNome(produtoAtualizacaoRequest.nome());
        }

        if(produtoAtualizacaoRequest.descricao() != null) {
            produto.alterarDescricao(produtoAtualizacaoRequest.descricao());
        }

        if(produtoAtualizacaoRequest.preco() != null) {
            produto.alterarPreco(produtoAtualizacaoRequest.preco());
        }

        if(produtoAtualizacaoRequest.estoque() != null) {
            produto.ajustarEstoque(produtoAtualizacaoRequest.estoque());
        }

        produtoRepository.save(produto);
        return ProdutoResponseDTO.from(produto);
    }

    @Transactional
    public void removerProduto(Long id) {
        Produto produto = produtoRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Produto não encontrado")
                );

        produtoRepository.delete(produto);
        produtoRepository.flush();
    }

}
