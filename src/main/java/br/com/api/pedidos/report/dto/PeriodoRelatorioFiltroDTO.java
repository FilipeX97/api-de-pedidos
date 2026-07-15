package br.com.api.pedidos.report.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PeriodoRelatorioFiltroDTO(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataInicio,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataFim
) {
}
