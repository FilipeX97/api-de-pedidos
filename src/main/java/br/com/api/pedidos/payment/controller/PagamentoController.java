package br.com.api.pedidos.payment.controller;

import br.com.api.pedidos.payment.dto.PagamentoRequestDTO;
import br.com.api.pedidos.payment.dto.PagamentoResponseDTO;
import br.com.api.pedidos.payment.facade.CheckoutFacade;
import br.com.api.pedidos.security.service.UsuarioLogadoService;
import br.com.api.pedidos.shared.idempotency.service.IdempotencyService;
import br.com.api.pedidos.shared.response.RespostaApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders/{idPedido}/payments")
public class PagamentoController {

    private final CheckoutFacade checkoutFacade;
    private final UsuarioLogadoService usuarioLogadoService;
    private final IdempotencyService idempotencyService;

    public PagamentoController(
            CheckoutFacade checkoutFacade,
            UsuarioLogadoService usuarioLogadoService,
            IdempotencyService idempotencyService) {
        this.checkoutFacade = checkoutFacade;
        this.usuarioLogadoService = usuarioLogadoService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaApi<PagamentoResponseDTO> processarPagamento(
            @RequestHeader("Idempotency-Key") String key,
            HttpServletRequest request,
            @PathVariable Long idPedido,
            @RequestBody PagamentoRequestDTO pagamentoRequestDTO) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return idempotencyService.executar(
                key,
                request,
                pagamentoRequestDTO,
                PagamentoResponseDTO.class,
                () -> checkoutFacade.processarPagamento(
                        idPedido,
                        usuario,
                        pagamentoRequestDTO
                ),
                "Pagamento processado com sucesso"
        );
    }

    @GetMapping
    public RespostaApi<List<PagamentoResponseDTO>> listarPagamentosDoPedido(
            @PathVariable Long idPedido) {
        var usuario = usuarioLogadoService.getUsuarioLogado();

        return RespostaApi.sucesso(
                checkoutFacade.listarPagamentosDoPedido(idPedido, usuario),
                "Pagamentos encontrados"
        );
    }

}
