package br.com.api.pedidos.coupon.service;

import br.com.api.pedidos.coupon.dto.CupomRequestDTO;
import br.com.api.pedidos.coupon.dto.CupomResponseDTO;
import br.com.api.pedidos.coupon.entity.Cupom;
import br.com.api.pedidos.coupon.repository.CupomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CupomService {

    private final CupomRepository cupomRepository;

    public CupomService(CupomRepository cupomRepository) {
        this.cupomRepository = cupomRepository;
    }

    public CupomResponseDTO criarCupom(CupomRequestDTO cupomRequestDTO) {
        if(cupomRepository.findByCodigoIgnoreCase(cupomRequestDTO.codigo()).isPresent()) {
            throw new RuntimeException("Código de cupom já existe");
        }

        var cupom = new Cupom(
                cupomRequestDTO.codigo(),
                cupomRequestDTO.percentual(),
                cupomRequestDTO.dataInicio(),
                cupomRequestDTO.dataFim(),
                cupomRequestDTO.limiteUso()
        );

        cupomRepository.save(cupom);
        return CupomResponseDTO.from(cupom);
    }

    public List<CupomResponseDTO> listarCupons() {
        return cupomRepository.findAll()
                .stream()
                .map(CupomResponseDTO::from)
                .toList();
    }

    public CupomResponseDTO buscarCupomPorId(Long id) {
        return CupomResponseDTO.from(buscarPorId(id));
    }

    public CupomResponseDTO ativarCupom(Long id) {
        var cupom = buscarPorId(id);
        cupom.ativar();
        cupomRepository.save(cupom);
        return CupomResponseDTO.from(cupom);
    }

    public CupomResponseDTO desativarCupom(Long id) {
        var cupom = buscarPorId(id);
        cupom.desativar();
        cupomRepository.save(cupom);
        return CupomResponseDTO.from(cupom);
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

    private Cupom buscarPorId(Long id) {
        return cupomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cupom não encontrado"));
    }
}
