package br.com.api.pedidos.payment.dto;

import br.com.api.pedidos.payment.entity.FormaPagamento;
import jakarta.validation.constraints.NotNull;

public record PagamentoRequestDTO(
        @NotNull(message = "Forma de pagamento é obrigatória")
        FormaPagamento formaPagamento
) {
}
