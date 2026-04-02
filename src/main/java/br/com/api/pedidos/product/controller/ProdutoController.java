package br.com.api.pedidos.product.controller;

import br.com.api.pedidos.product.dto.ProdutoRequestDTO;
import br.com.api.pedidos.product.dto.ProdutoResponseDTO;
import br.com.api.pedidos.product.service.ProdutoService;
import br.com.api.pedidos.security.userdetails.UsuarioSecurity;
import br.com.api.pedidos.shared.idempotency.service.IdempotencyService;
import br.com.api.pedidos.shared.response.RespostaApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public ProdutoController(
            ProdutoService produtoService,
            IdempotencyService idempotencyService,
            ObjectMapper objectMapper) {
        this.produtoService = produtoService;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaApi<ProdutoResponseDTO> criarProduto(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody ProdutoRequestDTO dto,
            HttpServletRequest request
    ) {
        return idempotencyService.executar(
                key,
                request,
                dto,
                ProdutoResponseDTO.class,
                () -> produtoService.criarProduto(dto),
                "Produto criado com sucesso"
        );
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
