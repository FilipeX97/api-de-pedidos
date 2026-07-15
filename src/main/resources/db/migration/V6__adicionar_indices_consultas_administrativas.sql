CREATE INDEX idx_pedido_status_data_criacao
    ON pedido (status, data_criacao);

CREATE INDEX idx_pedido_data_criacao
    ON pedido (data_criacao);

CREATE INDEX idx_pedido_cupom
    ON pedido (cupom_id);

CREATE INDEX idx_pedido_valor_final
    ON pedido (valor_final);