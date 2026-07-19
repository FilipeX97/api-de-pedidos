package br.com.api.pedidos.product.dto;

import br.com.api.pedidos.product.entity.Produto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        name = "ProdutoResponse",
        description = "Dados retornados pela API para representar um produto"
)
public record ProdutoResponseDTO(
        @Schema(
                description = "Identificador único do produto",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Nome comercial do produto",
                example = "Notebook Dell Inspiron 15"
        )
        String nome,

        @Schema(
                description = "Descrição detalhada do produto",
                example = "Notebook com 16 GB de memória RAM e SSD de 512 GB",
                nullable = true
        )
        String descricao,

        @Schema(
                description = "Preço unitário do produto",
                example = "3499.90"
        )
        BigDecimal preco,

        @Schema(
                description = "Quantidade disponível em estoque",
                example = "25"
        )
        Integer estoque,

        @Schema(
                description = "Indica se o produto está ativo",
                example = "true"
        )
        Boolean ativo
) {
    public static ProdutoResponseDTO from(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.getAtivo()
        );
    }
}
