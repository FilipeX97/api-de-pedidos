package br.com.api.pedidos.order.query.service;

import br.com.api.pedidos.order.entity.Pedido;
import br.com.api.pedidos.order.query.dto.PedidoFiltroDTO;
import br.com.api.pedidos.order.query.dto.PedidoResumoResponseDTO;
import br.com.api.pedidos.order.query.specification.PedidoSpecifications;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.pagination.util.PaginacaoUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PedidoConsultaService {

    // Chave e o nome aceito na url
    // Valor e o nome do atributo existente na entidade
    private static final Map<String, String> CAMPOS_ORDENACAO =
            Map.ofEntries(
                    Map.entry("idPedido", "id"),
                    Map.entry("nomeCliente", "usuario.nome"),
                    Map.entry("emailCliente", "usuario.email"),
                    Map.entry("status", "status"),
                    Map.entry("valorBruto", "valorBruto"),
                    Map.entry("valorDesconto", "valorDesconto"),
                    Map.entry("valorFinal", "valorFinal"),
                    Map.entry("codigoCupom", "cupom.codigo"),
                    Map.entry("dataCriacao", "dataCriacao")
            );

    private static final Sort ORDENACAO_PADRAO =
            Sort.by(
                    Sort.Order.desc("dataCriacao")
            );

    private final PedidoRepository pedidoRepository;

    public PedidoConsultaService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<PedidoResumoResponseDTO> consultar(
            PedidoFiltroDTO filtro,
            Pageable pageable
    ) {
        validarFiltro(filtro);

        Pageable pageableValidado =
                PaginacaoUtils.normalizar(
                        pageable,
                        CAMPOS_ORDENACAO,
                        ORDENACAO_PADRAO
                );

        Specification<Pedido> specification =
                PedidoSpecifications.comFiltros(filtro);

        Page<PedidoResumoResponseDTO> resultado =
                pedidoRepository
                        .findAll(
                                specification,
                                pageableValidado
                        )
                        .map(PedidoResumoResponseDTO::from);

        return PaginaResponseDTO.from(resultado);
    }

    private void validarFiltro(PedidoFiltroDTO filtro) {
        if (filtro == null) {
            throw new IllegalArgumentException(
                    "Filtro da consulta é obrigatório"
            );
        }

        validarPeriodo(filtro);
        validarValores(filtro);
    }

    private void validarPeriodo(PedidoFiltroDTO filtro) {
        if (filtro.dataInicio() == null
                || filtro.dataFim() == null) {
            return;
        }

        if (filtro.dataInicio().isAfter(filtro.dataFim())) {
            throw new IllegalArgumentException(
                    "Data inicial não pode ser posterior à data final"
            );
        }
    }

    private void validarValores(PedidoFiltroDTO filtro) {
        if (filtro.valorMinimo() == null
                || filtro.valorMaximo() == null) {
            return;
        }

        if (filtro.valorMinimo()
                .compareTo(filtro.valorMaximo()) > 0) {
            throw new IllegalArgumentException(
                    "Valor mínimo não pode ser maior que o valor máximo"
            );
        }
    }
}
