package br.com.api.pedidos.coupon.dto;

import br.com.api.pedidos.coupon.entity.Cupom;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(
        name = "CupomResponse",
        description = "Dados retornados pela API para representar um cupom"
)
public record CupomResponseDTO(
        @Schema(
                description = "Identificador único do cupom",
                example = "5"
        )
        Long id,

        @Schema(
                description = "Código utilizado para aplicar o cupom",
                example = "DESCONTO15"
        )
        String codigo,

        @Schema(
                description = """
                        Percentual de desconto representado em formato decimal.

                        O valor 0.15 representa desconto de 15%.
                        """,
                example = "0.15"
        )
        BigDecimal percentual,

        @Schema(
                description = "Data e hora de início da validade",
                example = "2026-08-01T00:00:00",
                format = "date-time"
        )
        LocalDateTime dataInicio,

        @Schema(
                description = "Data e hora de encerramento da validade",
                example = "2026-12-31T23:59:59",
                format = "date-time"
        )
        LocalDateTime dataFim,

        @Schema(
                description = "Indica se o cupom está ativo",
                example = "true"
        )
        boolean ativo,

        @Schema(
                description = "Quantidade máxima de utilizações permitidas",
                example = "100"
        )
        Integer limiteUso,

        @Schema(
                description = "Quantidade de vezes que o cupom já foi utilizado",
                example = "12"
        )
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
