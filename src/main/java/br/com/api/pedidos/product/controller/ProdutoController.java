package br.com.api.pedidos.product.controller;

import br.com.api.pedidos.product.dto.ProdutoRequestDTO;
import br.com.api.pedidos.product.dto.ProdutoResponseDTO;
import br.com.api.pedidos.product.service.ProdutoService;
import br.com.api.pedidos.shared.idempotency.service.IdempotencyService;
import br.com.api.pedidos.shared.response.RespostaApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RespostaApi<ProdutoResponseDTO> criarProduto(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody ProdutoRequestDTO produtoRequestDTO) {
        var respostaCache = idempotencyService.buscarResposta(key);

        if(respostaCache.isPresent()) {
            try {
                return objectMapper.readValue(respostaCache.get(), RespostaApi.class);
            } catch (JsonProcessingException e) {
                return RespostaApi.erro(
                        "Erro ao criar produto"
                );
            }
        }

        var resposta = RespostaApi.sucesso(
                produtoService.criarProduto(produtoRequestDTO),
                "Produto criado com sucesso"
        );

        idempotencyService.salvarResposta(key, resposta);
        return resposta;
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
