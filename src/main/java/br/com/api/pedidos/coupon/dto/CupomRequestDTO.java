package br.com.api.pedidos.coupon.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CupomRequestDTO(
        String codigo,
        BigDecimal percentual,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        Integer limiteUso
) {}
