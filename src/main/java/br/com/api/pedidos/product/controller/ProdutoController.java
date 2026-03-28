package br.com.api.pedidos.product.controller;

import br.com.api.pedidos.product.dto.ProdutoRequestDTO;
import br.com.api.pedidos.product.dto.ProdutoResponseDTO;
import br.com.api.pedidos.product.service.ProdutoService;
import br.com.api.pedidos.shared.response.RespostaApi;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/{id}")
    public RespostaApi<ProdutoResponseDTO> buscarProdutoPorId(@PathVariable Long id) {
        return RespostaApi.sucesso(produtoService.buscarProdutoPorId(id),
                "Produto encontrado");
    }

    @GetMapping
    public RespostaApi<List<ProdutoResponseDTO>> listarProdutos() {
        return RespostaApi.sucesso(produtoService.listarProdutos(),
                "Lista de produtos");
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RespostaApi<ProdutoResponseDTO> criarProduto(@RequestBody ProdutoRequestDTO produtoRequestDTO) {
        return RespostaApi.sucesso(produtoService.criarProduto(produtoRequestDTO),
                "Produto criado com sucesso");
    }

    @PatchMapping("/{id}")
    public RespostaApi<ProdutoResponseDTO> atualizarProduto(@PathVariable Long id, @RequestBody ProdutoRequestDTO produtoRequestDTO) {
        return RespostaApi.sucesso(produtoService.atualizarProduto(id, produtoRequestDTO),
                "Produto atualizado");
    }

    @DeleteMapping("/{id}")
    public RespostaApi<Void> removerProduto(@PathVariable Long id) {
        produtoService.removerProduto(id);
        return RespostaApi.sucesso(null, "Produto removido com sucesso");
    }
}
