package br.com.api.pedidos.order.controller;

import br.com.api.pedidos.order.dto.AdicionarPedidoRequestDTO;
import br.com.api.pedidos.order.dto.AlterarQuantidadeItemRequestDTO;
import br.com.api.pedidos.order.dto.AplicarCupomRequestDTO;
import br.com.api.pedidos.order.dto.PedidoResponseDTO;
import br.com.api.pedidos.order.service.PedidoService;
import br.com.api.pedidos.security.service.UsuarioLogadoService;
import br.com.api.pedidos.shared.response.RespostaApi;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioLogadoService usuarioLogadoService;

    public PedidoController(
            PedidoService pedidoService,
            UsuarioLogadoService usuarioLogadoService
    ) {
        this.pedidoService = pedidoService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    @GetMapping
    public RespostaApi<List<PedidoResponseDTO>> buscarTodosPedidos() {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                pedidoService.buscarTodosPedidos(usuario),
                "Pedidos encontrados"
        );
    }

    @GetMapping("/{id}")
    public RespostaApi<PedidoResponseDTO> buscarPedidoPorId(@PathVariable Long id) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                pedidoService.buscarPedidoPorId(id, usuario),
                "Pedido encontrado"
        );
    }

    @PostMapping
    public RespostaApi<PedidoResponseDTO> criarPedido() {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                pedidoService.criarPedido(usuario),
                "Pedido criado com sucesso"
        );
    }

    @PostMapping("/{idPedido}/items")
    public RespostaApi<PedidoResponseDTO> adicionarItemPedido(
            @PathVariable Long idPedido,
            @RequestBody AdicionarPedidoRequestDTO adicionarPedidoRequestDTO) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                pedidoService.adicionarItemPedido(idPedido, adicionarPedidoRequestDTO, usuario),
                "Item adicionado ao pedido com sucesso"
        );
    }

    @PatchMapping("/{idPedido}/items/{itemId}")
    public RespostaApi<PedidoResponseDTO> alterarQuantidadeItemPedido(
            @PathVariable Long idPedido,
            @PathVariable Long itemId,
            @RequestBody AlterarQuantidadeItemRequestDTO dto) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                pedidoService.alterarQuantidadeItemPedido(
                        idPedido,
                        itemId,
                        dto,
                        usuario
                ),
                "Quantidade alterada com sucesso"
        );
    }

    @DeleteMapping("/{idPedido}/items/{itemId}")
    public RespostaApi<PedidoResponseDTO> removerItemPedido(
            @PathVariable Long idPedido,
            @PathVariable Long itemId) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                pedidoService.removerItemPedido(idPedido, itemId, usuario),
                "Item removido do pedido com sucesso"
        );
    }

    @PostMapping("/{idPedido}/coupon")
    public RespostaApi<PedidoResponseDTO> aplicarCupom(
            @PathVariable Long idPedido,
            @RequestBody AplicarCupomRequestDTO dto) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                pedidoService.aplicarCupom(
                        idPedido,
                        usuario,
                        dto.codigoCupom()),
                "Cupom aplicado com sucesso"
        );
    }

    @PostMapping("/{idPedido}/pay")
    public RespostaApi<PedidoResponseDTO> pagarPedido(
            @PathVariable Long idPedido) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                pedidoService.pagarPedido(idPedido, usuario),
                "Pedido pago com sucesso"
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{idPedido}/ship")
    public RespostaApi<PedidoResponseDTO> enviarPedido(
            @PathVariable Long idPedido) {
        return RespostaApi.sucesso(
                pedidoService.enviarPedido(idPedido),
                "Pedido enviado com sucesso"
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{idPedido}/deliver")
    public RespostaApi<PedidoResponseDTO> entregarPedido(
            @PathVariable Long idPedido) {
        return RespostaApi.sucesso(
                pedidoService.entregarPedido(idPedido),
                "Pedido entregue com sucesso"
        );
    }

    @PostMapping("/{idPedido}/cancel")
    public RespostaApi<PedidoResponseDTO> cancelarPedido(
            @PathVariable Long idPedido) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                pedidoService.cancelarPedido(idPedido, usuario),
                "Cancelamento do pedido realizado com sucesso"
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{idPedido}/refund")
    public RespostaApi<PedidoResponseDTO> estornarPedido(
            @PathVariable Long idPedido) {
        return RespostaApi.sucesso(
                pedidoService.estornarPedido(idPedido),
                "Pedido estornado com sucesso"
        );
    }

}
