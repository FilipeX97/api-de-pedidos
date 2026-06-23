package br.com.api.pedidos.coupon.dto;

import br.com.api.pedidos.coupon.entity.Cupom;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CupomResponseDTO(
        Long id,
        String codigo,
        BigDecimal percentual,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        boolean ativo,
        Integer limiteUso,
        Integer quantidadeDeUso
) {
    public static CupomResponseDTO from(Cupom cupom) {
        return new CupomResponseDTO(
                cupom.getId(),
                cupom.getCodigo(),
                cupom.getPercentual(),
                cupom.getDataInicio(),
                cupom.getDataFim(),
                cupom.isAtivo(),
                cupom.getLimiteUso(),
                cupom.getQuantidadeDeUso()
        );
    }
}
