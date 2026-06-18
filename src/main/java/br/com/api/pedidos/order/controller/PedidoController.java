package br.com.api.pedidos.order.controller;

import br.com.api.pedidos.order.dto.AdicionarPedidoRequestDTO;
import br.com.api.pedidos.order.dto.AlterarQuantidadeItemRequestDTO;
import br.com.api.pedidos.order.dto.PedidoResponseDTO;
import br.com.api.pedidos.order.service.PedidoService;
import br.com.api.pedidos.security.service.UsuarioLogadoService;
import br.com.api.pedidos.shared.response.RespostaApi;
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
    public RespostaApi<PedidoResponseDTO>
    alterarQuantidadeItemPedido(
            @PathVariable Long idPedido,
            @PathVariable Long itemId,
            @RequestBody AlterarQuantidadeItemRequestDTO dto
    ) {
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

}
