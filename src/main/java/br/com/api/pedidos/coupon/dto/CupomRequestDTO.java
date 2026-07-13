package br.com.api.pedidos.coupon.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CupomRequestDTO(
        @NotBlank(message = "Código do cupom é obrigatório")
        String codigo,

        @NotNull(message = "Percentual é obrigatório")
        @DecimalMin(value = "0.01", message = "Percentual deve ser maior que zero")
        @DecimalMax(value = "1.00", message = "Percentual deve ser menor ou igual a 1")
        BigDecimal percentual,

        @NotNull(message = "Data de início é obrigatória")
        LocalDateTime dataInicio,

        @NotNull(message = "Data de fim é obrigatória")
        @Future(message = "Data de fim deve ser futura")
        LocalDateTime dataFim,

        @NotNull(message = "Limite de uso é obrigatório")
        @Positive(message = "Limite de uso deve ser maior que zero")
        Integer limiteUso
) {}
