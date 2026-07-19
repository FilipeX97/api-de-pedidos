package br.com.api.pedidos.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(
        name = "ProdutoAtualizacaoRequest",
        description = """
                Campos que podem ser alterados em um produto.

                Como a atualização é parcial, somente os campos enviados
                serão modificados.
                """
)
public record ProdutoAtualizacaoRequest(
        @Schema(
                description = "Novo nome comercial do produto",
                example = "Notebook Dell Inspiron 15 Plus",
                nullable = true,
                minLength = 1,
                maxLength = 100
        )
        @Pattern(
                regexp = ".*\\S.*",
                message = "Nome não pode estar em branco"
        )
        @Size(
                max = 100,
                message = "Nome deve ter no máximo 100 caracteres"
        )
        String nome,

        @Schema(
                description = "Nova descrição do produto",
                example = "Notebook com processador atualizado",
                nullable = true,
                maxLength = 500
        )
        @Size(
                max = 500,
                message = "Descrição deve ter no máximo 500 caracteres"
        )
        String descricao,

        @Schema(
                description = "Novo preço unitário do produto",
                example = "3299.90",
                nullable = true,
                minimum = "0.01"
        )
        @DecimalMin(
                value = "0.01",
                message = "Preço deve ser maior que zero"
        )
        BigDecimal preco,

        @Schema(
                description = "Nova quantidade total em estoque",
                example = "30",
                nullable = true,
                minimum = "0"
        )
        @Min(
                value = 0,
                message = "Estoque não pode ser negativo"
        )
        Integer estoque
) {
}
