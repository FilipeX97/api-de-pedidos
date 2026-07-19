package br.com.api.pedidos.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(
        name = "PeriodoRelatorioFiltro",
        description = """
                Período opcional utilizado na geração do resumo de pedidos.

                Quando nenhuma data é informada, o relatório considera
                todos os pedidos cadastrados.

                Quando somente a data inicial é informada, são considerados
                os pedidos criados a partir dessa data.

                Quando somente a data final é informada, são considerados
                os pedidos criados até o final dessa data.
                """
)
public record PeriodoRelatorioFiltroDTO(
        @Schema(
                description = """
                        Data inicial inclusiva do período considerado
                        pelo relatório
                        """,
                example = "2026-07-01",
                format = "date",
                nullable = true
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataInicio,

        @Schema(
                description = """
                        Data final inclusiva do período considerado
                        pelo relatório
                        """,
                example = "2026-07-31",
                format = "date",
                nullable = true
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataFim
) {
}
