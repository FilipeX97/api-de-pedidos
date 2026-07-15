CREATE INDEX idx_pedido_usuario_data_criacao
    ON pedido (usuario_id, data_criacao);

CREATE INDEX idx_notificacao_usuario_data_criacao
    ON notificacao (usuario_id, data_criacao);

CREATE INDEX idx_notificacao_usuario_lida_data_criacao
    ON notificacao (usuario_id, lida, data_criacao);