package br.com.api.pedidos.order.query.controller;

import br.com.api.pedidos.order.query.dto.PedidoFiltroDTO;
import br.com.api.pedidos.order.query.dto.PedidoResumoResponseDTO;
import br.com.api.pedidos.order.query.service.PedidoConsultaService;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.response.RespostaApi;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPedidoConsultaController {

    private final PedidoConsultaService pedidoConsultaService;

    public AdminPedidoConsultaController(
            PedidoConsultaService pedidoConsultaService
    ) {
        this.pedidoConsultaService = pedidoConsultaService;
    }

    @GetMapping
    public RespostaApi<PaginaResponseDTO<PedidoResumoResponseDTO>> consultarPedido(
            @Valid @ModelAttribute PedidoFiltroDTO pedidoFiltroDTO,
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "dataCriacao",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
            ) {
        return RespostaApi.sucesso(
                pedidoConsultaService.consultar(
                        pedidoFiltroDTO,
                        pageable
                ),
                "Pedidos encontrados"
        );
    }

}
