package br.com.api.pedidos.order.dto;

import jakarta.validation.constraints.NotBlank;

public record AplicarCupomRequestDTO(
        @NotBlank(message = "Código do cupom é obrigatório")
        String codigoCupom
) {
}
