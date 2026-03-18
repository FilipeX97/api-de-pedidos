package br.com.api.pedidos.product.controller;

import br.com.api.pedidos.product.dto.ProdutoRequestDTO;
import br.com.api.pedidos.product.dto.ProdutoResponseDTO;
import br.com.api.pedidos.product.service.ProdutoService;
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
    public ProdutoResponseDTO buscarProdutoPorId(@PathVariable Long id) {
        return produtoService.buscarProdutoPorId(id);
    }

    @GetMapping
    public List<ProdutoResponseDTO> listarProdutos() {
        return produtoService.listarProdutos();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ProdutoResponseDTO criarProduto(@RequestBody ProdutoRequestDTO produtoRequestDTO) {
        return produtoService.criarProduto(produtoRequestDTO);
    }

    @PatchMapping("/{id}")
    public ProdutoResponseDTO atualizarProduto(@PathVariable Long id, @RequestBody ProdutoRequestDTO produtoRequestDTO) {
        return produtoService.atualizarProduto(id, produtoRequestDTO);
    }

    @DeleteMapping("/{id}")
    public void removerProduto(@PathVariable Long id) {
        produtoService.removerProduto(id);
    }
}
