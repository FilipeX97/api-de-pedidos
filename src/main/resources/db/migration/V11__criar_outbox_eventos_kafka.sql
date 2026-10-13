CREATE TABLE outbox_evento
(
    id              UUID         NOT NULL,
    tipo_evento     VARCHAR(100) NOT NULL,
    chave_evento    VARCHAR(100) NOT NULL,
    topico          VARCHAR(255) NOT NULL,
    payload         TEXT         NOT NULL,
    status          VARCHAR(30)  NOT NULL,
    tentativas      INTEGER      NOT NULL,
    data_criacao    TIMESTAMP    NOT NULL,
    data_publicacao TIMESTAMP,
    ultima_tentativa TIMESTAMP,
    ultima_excecao  VARCHAR(2000),

    CONSTRAINT pk_outbox_evento PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_evento_status_data ON outbox_evento (status, data_criacao);
CREATE INDEX idx_outbox_evento_chave_evento ON outbox_evento (chave_evento);