package br.com.api.pedidos.coupon.service;

import br.com.api.pedidos.coupon.dto.CupomRequestDTO;
import br.com.api.pedidos.coupon.dto.CupomResponseDTO;
import br.com.api.pedidos.coupon.entity.Cupom;
import br.com.api.pedidos.coupon.repository.CupomRepository;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.pagination.util.PaginacaoUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class CupomService {

    private static final Map<String, String> CAMPOS_ORDENACAO =
            Map.ofEntries(
                    Map.entry("id", "id"),
                    Map.entry("codigo", "codigo"),
                    Map.entry("percentual", "percentual"),
                    Map.entry("dataInicio", "dataInicio"),
                    Map.entry("dataFim", "dataFim"),
                    Map.entry("ativo", "ativo"),
                    Map.entry("limiteUso", "limiteUso"),
                    Map.entry(
                            "quantidadeDeUso",
                            "quantidadeDeUso"
                    )
            );

    private static final Sort ORDENACAO_PADRAO =
            Sort.by(
                    Sort.Order.desc("dataFim")
            );

    private final CupomRepository cupomRepository;

    public CupomService(CupomRepository cupomRepository) {
        this.cupomRepository = cupomRepository;
    }

    @Transactional
    public CupomResponseDTO criarCupom(CupomRequestDTO cupomRequestDTO) {
        if (cupomRepository.findByCodigoIgnoreCase(cupomRequestDTO.codigo()).isPresent()) {
            throw new RuntimeException("Código de cupom já existe");
        }

        Cupom cupom = new Cupom(
                cupomRequestDTO.codigo(),
                cupomRequestDTO.percentual(),
                cupomRequestDTO.dataInicio(),
                cupomRequestDTO.dataFim(),
                cupomRequestDTO.limiteUso()
        );

        Cupom cupomSalvo = cupomRepository.saveAndFlush(cupom);
        return CupomResponseDTO.from(cupomSalvo);
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<CupomResponseDTO> listarCupons(
            Pageable pageable
    ) {
        Pageable pageableValidado =
                PaginacaoUtils.normalizar(
                        pageable,
                        CAMPOS_ORDENACAO,
                        ORDENACAO_PADRAO
                );

        var pagina = cupomRepository
                .findAll(pageableValidado)
                .map(CupomResponseDTO::from);

        return PaginaResponseDTO.from(pagina);
    }

    @Transactional(readOnly = true)
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
