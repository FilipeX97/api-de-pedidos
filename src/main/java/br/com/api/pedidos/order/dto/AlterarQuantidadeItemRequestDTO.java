package br.com.api.pedidos.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlterarQuantidadeItemRequestDTO(
        @NotNull(message = "Nova quantidade é obrigatória")
        @Positive(message = "Nova quantidade deve ser maior que zero")
        Integer novaQuantidade
) {
}
