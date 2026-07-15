package br.com.api.pedidos.shared.pagination.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaginaResponseDTO<T>(
        List<T> conteudo,
        Integer paginaAtual,
        Integer totalPaginas,
        Long totalElementos,
        Integer tamanhoPagina,
        Integer quantidadeElementos,
        Boolean primeiraPagina,
        Boolean ultimaPagina,
        Boolean vazia
) {
    public static <T> PaginaResponseDTO<T> from(Page<T> pagina) {
        return new PaginaResponseDTO<>(
                List.copyOf(pagina.getContent()),
                pagina.getNumber(),
                pagina.getTotalPages(),
                pagina.getTotalElements(),
                pagina.getSize(),
                pagina.getNumberOfElements(),
                pagina.isFirst(),
                pagina.isLast(),
                pagina.isEmpty()
        );
    }
}
