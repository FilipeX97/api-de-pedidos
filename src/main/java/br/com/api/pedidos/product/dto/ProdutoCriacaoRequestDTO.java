package br.com.api.pedidos.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(
        name = "ProdutoCriacaoRequest",
        description = "Dados necessários para cadastrar um novo produto"
)
public record ProdutoCriacaoRequestDTO(
        @Schema(
                description = "Nome comercial do produto",
                example = "Notebook Dell Inspiron 15",
                minLength = 1,
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Nome é obrigatório")
        @Size(
                max = 100,
                message = "Nome deve ter no máximo 100 caracteres"
        )
        String nome,

        @Schema(
                description = "Descrição detalhada do produto",
                example = "Notebook com 16 GB de memória RAM e SSD de 512 GB",
                nullable = true,
                maxLength = 500
        )
        @Size(
                max = 500,
                message = "Descrição deve ter no máximo 500 caracteres"
        )
        String descricao,

        @Schema(
                description = "Preço unitário do produto",
                example = "3499.90",
                minimum = "0.01",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(
                value = "0.01",
                message = "Preço deve ser maior que zero"
        )
        BigDecimal preco,

        @Schema(
                description = "Quantidade inicial disponível em estoque",
                example = "25",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Estoque é obrigatório")
        @Min(
                value = 0,
                message = "Estoque não pode ser negativo"
        )
        Integer estoque
) {
}
