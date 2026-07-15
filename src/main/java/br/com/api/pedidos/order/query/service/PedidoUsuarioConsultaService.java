package br.com.api.pedidos.order.query.service;

import br.com.api.pedidos.order.query.dto.PedidoUsuarioResumoResponseDTO;
import br.com.api.pedidos.order.repository.PedidoRepository;
import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.pagination.util.PaginacaoUtils;
import br.com.api.pedidos.user.entity.Usuario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class PedidoUsuarioConsultaService {

    private static final Map<String, String> CAMPOS_ORDENACAO =
            Map.ofEntries(
                    Map.entry("idPedido", "id"),
                    Map.entry("dataCriacao", "dataCriacao"),
                    Map.entry("status", "status"),
                    Map.entry("valorBruto", "valorBruto"),
                    Map.entry("valorDesconto", "valorDesconto"),
                    Map.entry("valorFinal", "valorFinal"),
                    Map.entry("codigoCupom", "cupom.codigo")
            );

    private static final Sort ORDENACAO_PADRAO =
            Sort.by(
                    Sort.Order.desc("dataCriacao")
            );

    private final PedidoRepository pedidoRepository;

    public PedidoUsuarioConsultaService(
            PedidoRepository pedidoRepository
    ) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<PedidoUsuarioResumoResponseDTO>
    listarPedidosDoUsuario(
            Usuario usuario,
            Pageable pageable
    ) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }

        Pageable pageableValidado =
                PaginacaoUtils.normalizar(
                        pageable,
                        CAMPOS_ORDENACAO,
                        ORDENACAO_PADRAO
                );

        var pagina = pedidoRepository
                .findAllByUsuario(
                        usuario,
                        pageableValidado
                )
                .map(PedidoUsuarioResumoResponseDTO::from);

        return PaginaResponseDTO.from(pagina);
    }
}
