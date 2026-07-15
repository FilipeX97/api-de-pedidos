package br.com.api.pedidos.report.controller;

import br.com.api.pedidos.report.dto.PeriodoRelatorioFiltroDTO;
import br.com.api.pedidos.report.dto.ResumoPedidosResponseDTO;
import br.com.api.pedidos.report.service.RelatorioPedidoService;
import br.com.api.pedidos.shared.response.RespostaApi;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reports/orders")
@PreAuthorize("hasRole('ADMIN')")
public class RelatorioPedidoController {

    private final RelatorioPedidoService relatorioPedidoService;

    public RelatorioPedidoController(
            RelatorioPedidoService relatorioPedidoService
    ) {
        this.relatorioPedidoService = relatorioPedidoService;
    }

    @GetMapping("/summary")
    public RespostaApi<ResumoPedidosResponseDTO> gerarResumo(
            @Valid
            @ModelAttribute
            PeriodoRelatorioFiltroDTO filtro
    ) {
        return RespostaApi.sucesso(
                relatorioPedidoService.gerarResumo(filtro),
                "Resumo dos pedidos gerado com sucesso"
        );
    }

}
