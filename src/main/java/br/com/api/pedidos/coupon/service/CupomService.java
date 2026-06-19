package br.com.api.pedidos.coupon.service;

import br.com.api.pedidos.coupon.entity.Cupom;
import br.com.api.pedidos.coupon.repository.CupomRepository;
import org.springframework.stereotype.Service;

@Service
public class CupomService {

    private final CupomRepository cupomRepository;

    public CupomService(CupomRepository cupomRepository) {
        this.cupomRepository = cupomRepository;
    }

    public Cupom buscarCupomValido(String codigo) {
        Cupom cupom = cupomRepository
                .findByCodigoIgnoreCase(codigo)
                .orElseThrow(() ->
                        new RuntimeException("Cupom inválido")
                );

        if (!cupom.podeSerUtilizado()) {
            throw new RuntimeException("Cupom expirado");
        }

        return cupom;
    }
}
