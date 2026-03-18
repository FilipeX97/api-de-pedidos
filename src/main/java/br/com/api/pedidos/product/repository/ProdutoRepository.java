package br.com.api.pedidos.product.repository;

import br.com.api.pedidos.product.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
