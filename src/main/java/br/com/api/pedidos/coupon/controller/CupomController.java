package br.com.api.pedidos.coupon.controller;

import br.com.api.pedidos.coupon.dto.CupomRequestDTO;
import br.com.api.pedidos.coupon.dto.CupomResponseDTO;
import br.com.api.pedidos.coupon.service.CupomService;
import br.com.api.pedidos.shared.idempotency.service.IdempotencyService;
import br.com.api.pedidos.shared.response.RespostaApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cupons")
@PreAuthorize("hasRole('ADMIN')")
public class CupomController {

    private final CupomService cupomService;
    private final IdempotencyService idempotencyService;

    public CupomController(
            CupomService cupomService,
            IdempotencyService idempotencyService) {
        this.cupomService = cupomService;
        this.idempotencyService = idempotencyService;
    };

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaApi<CupomResponseDTO> criarCupom(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody CupomRequestDTO cupomRequestDTO,
            HttpServletRequest request) {
        return idempotencyService.executar(
                key,
                request,
                cupomRequestDTO,
                CupomResponseDTO.class,
                () -> cupomService.criarCupom(cupomRequestDTO),
                "Cupom criado com sucesso"
        );
    }

    @GetMapping
    public RespostaApi<List<CupomResponseDTO>> listarCupons() {
        return RespostaApi.sucesso(
                cupomService.listarCupons(),
                "Cupons encontrados"
        );
    }

    @GetMapping("/{id}")
    public RespostaApi<CupomResponseDTO> buscarCupomPorId(@PathVariable Long id) {
        return RespostaApi.sucesso(
                cupomService.buscarCupomPorId(id),
                "Cupom encontrado"
        );
    }

    @PostMapping("/{id}/ativar")
    public RespostaApi<CupomResponseDTO> ativarCupom(@PathVariable Long id) {
        return RespostaApi.sucesso(
                cupomService.ativarCupom(id),
                "Cupom ativado com sucesso"
        );
    }

    @PostMapping("/{id}/desativar")
    public RespostaApi<CupomResponseDTO> desativarCupom(@PathVariable Long id) {
        return RespostaApi.sucesso(
                cupomService.desativarCupom(id),
                "Cupom desativado com sucesso"
        );
    }
}
