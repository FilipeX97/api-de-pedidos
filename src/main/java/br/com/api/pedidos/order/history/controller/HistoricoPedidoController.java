package br.com.api.pedidos.order.history.controller;

import br.com.api.pedidos.order.history.dto.HistoricoPedidoResponseDTO;
import br.com.api.pedidos.order.history.service.HistoricoPedidoService;
import br.com.api.pedidos.order.service.PedidoService;
import br.com.api.pedidos.security.service.UsuarioLogadoService;
import br.com.api.pedidos.shared.response.RespostaApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders/{idPedido}/history")
public class HistoricoPedidoController {

    private final HistoricoPedidoService historicoPedidoService;
    private final PedidoService pedidoService;
    private final UsuarioLogadoService usuarioLogadoService;

    public HistoricoPedidoController(
            HistoricoPedidoService historicoPedidoService,
            PedidoService pedidoService,
            UsuarioLogadoService usuarioLogadoService) {
        this.historicoPedidoService = historicoPedidoService;
        this.pedidoService = pedidoService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    @GetMapping
    public RespostaApi<List<HistoricoPedidoResponseDTO>> listarHistorico(
            @PathVariable Long idPedido) {
        var usuario = usuarioLogadoService.getUsuarioLogado();
        pedidoService.buscarPedidoPorId(idPedido, usuario);

        return RespostaApi.sucesso(
                historicoPedidoService.listarPorPedido(idPedido),
                "Histórico do pedido encontrado"
        );
    }
}
