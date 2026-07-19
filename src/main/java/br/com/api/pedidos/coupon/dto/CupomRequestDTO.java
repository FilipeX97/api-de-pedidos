package br.com.api.pedidos.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(
        name = "CupomRequest",
        description = "Dados necessários para cadastrar um novo cupom"
)
public record CupomRequestDTO(
        @Schema(
                description = """
                        Código utilizado pelo cliente para aplicar o cupom.

                        O código é armazenado em letras maiúsculas.
                        """,
                example = "DESCONTO15",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Código do cupom é obrigatório")
        String codigo,

        @Schema(
                description = """
                        Percentual de desconto representado por um valor
                        decimal entre 0.01 e 1.00.

                        Exemplo: 0.15 representa desconto de 15%.
                        """,
                example = "0.15",
                minimum = "0.01",
                maximum = "1.00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Percentual é obrigatório")
        @DecimalMin(
                value = "0.01",
                message = "Percentual deve ser maior que zero"
        )
        @DecimalMax(
                value = "1.00",
                message = "Percentual deve ser menor ou igual a 1"
        )
        BigDecimal percentual,

        @Schema(
                description = "Data e hora a partir da qual o cupom será válido",
                example = "2026-08-01T00:00:00",
                format = "date-time",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Data de início é obrigatória")
        LocalDateTime dataInicio,

        @Schema(
                description = """
                        Data e hora de encerramento da validade do cupom.

                        A data deve estar no futuro e não pode ser anterior
                        à data de início.
                        """,
                example = "2026-12-31T23:59:59",
                format = "date-time",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Data de fim é obrigatória")
        @Future(message = "Data de fim deve ser futura")
        LocalDateTime dataFim,

        @Schema(
                description = """
                        Quantidade máxima de utilizações permitidas
                        para o cupom
                        """,
                example = "100",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Limite de uso é obrigatório")
        @Positive(message = "Limite de uso deve ser maior que zero")
        Integer limiteUso
) {
}
