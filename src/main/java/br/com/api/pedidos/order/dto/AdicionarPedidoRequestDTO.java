package br.com.api.pedidos.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdicionarPedidoRequestDTO(
        @NotNull(message = "Id do produto é obrigatório")
        @Positive(message = "Id do produto deve ser maior que zero")
        Long idProduto,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Integer quantidade
) {
}
