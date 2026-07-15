package br.com.api.pedidos.coupon.controller;

import br.com.api.pedidos.coupon.dto.CupomRequestDTO;
import br.com.api.pedidos.coupon.dto.CupomResponseDTO;
import br.com.api.pedidos.coupon.service.CupomService;
import br.com.api.pedidos.shared.idempotency.service.IdempotencyService;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.response.RespostaApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaApi<CupomResponseDTO> criarCupom(
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody CupomRequestDTO cupomRequestDTO,
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
    public RespostaApi<PaginaResponseDTO<CupomResponseDTO>> listarCupons(
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "dataFim",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return RespostaApi.sucesso(
                cupomService.listarCupons(pageable),
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
    public RespostaApi<CupomResponseDTO> ativarCupom(
            @RequestHeader("Idempotency-Key") String key,
            HttpServletRequest request,
            @PathVariable Long id) {
        return idempotencyService.executar(
                key,
                request,
                null,
                CupomResponseDTO.class,
                () -> cupomService.ativarCupom(id),
                "Cupom ativado com sucesso"
        );
    }

    @PostMapping("/{id}/desativar")
    public RespostaApi<CupomResponseDTO> desativarCupom(
            @RequestHeader("Idempotency-Key") String key,
            HttpServletRequest request,
            @PathVariable Long id) {
        return idempotencyService.executar(
                key,
                request,
                null,
                CupomResponseDTO.class,
                () -> cupomService.desativarCupom(id),
                "Cupom desativado com sucesso"
        );
    }
}
