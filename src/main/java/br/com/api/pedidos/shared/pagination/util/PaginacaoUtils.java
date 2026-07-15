package br.com.api.pedidos.shared.pagination.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

public final class PaginacaoUtils {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    public PaginacaoUtils() {
    }

    public static Pageable normalizar(
            Pageable pageable,
            Map<String, String> camposPermitidos,
            Sort ordenacaoPadrao
    ) {
        validarPageable(pageable);
        validarCamposPermitidos(camposPermitidos);

        Sort ordenacaoNormalizada = normalizarOrdenacao(
                pageable.getSort(),
                camposPermitidos,
                ordenacaoPadrao
        );

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                ordenacaoNormalizada
        );
    }

    private static void validarPageable(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException(
                    "Dados de paginação são obrigatórios"
            );
        }

        if (pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException(
                    "Tamanho da página deve ser maior que zero"
            );
        }

        if (pageable.getPageSize() > TAMANHO_MAXIMO_PAGINA) {
            throw new IllegalArgumentException(
                    "Tamanho máximo da página é "
                            + TAMANHO_MAXIMO_PAGINA
            );
        }
    }

    private static void validarCamposPermitidos(
            Map<String, String> camposPermitidos
    ) {
        if (camposPermitidos == null || camposPermitidos.isEmpty()) {
            throw new IllegalArgumentException(
                    "Campos de ordenação permitidos não foram configurados"
            );
        }
    }

    private static Sort normalizarOrdenacao(
            Sort ordenacaoRecebida,
            Map<String, String> camposPermitidos,
            Sort ordenacaoPadrao
    ) {
        if (ordenacaoRecebida == null
                || ordenacaoRecebida.isUnsorted()) {
            return ordenacaoPadrao;
        }

        List<Sort.Order> ordens = ordenacaoRecebida
                .stream()
                .map(ordem ->
                        normalizarOrdem(
                                ordem,
                                camposPermitidos
                        )
                )
                .toList();

        return Sort.by(ordens);
    }

    private static Sort.Order normalizarOrdem(
            Sort.Order ordem,
            Map<String, String> camposPermitidos
    ) {
        String propriedadeEntidade =
                camposPermitidos.get(ordem.getProperty());

        if (propriedadeEntidade == null) {
            throw new IllegalArgumentException(
                    "Campo de ordenação inválido: "
                            + ordem.getProperty()
                            + ". Campos permitidos: "
                            + camposPermitidos.keySet()
            );
        }

        return ordem.withProperty(propriedadeEntidade);
    }
}
