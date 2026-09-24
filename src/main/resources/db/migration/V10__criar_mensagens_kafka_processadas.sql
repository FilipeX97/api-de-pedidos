CREATE TABLE mensagem_kafka_processada
(
    id_evento UUID NOT NULL,
    grupo_consumidor VARCHAR(100) NOT NULL,
    data_processamento TIMESTAMP NOT NULL,

    CONSTRAINT pk_mensagem_kafka_processada PRIMARY KEY (id_evento, grupo_consumidor)
);

CREATE INDEX idx_mensagem_kafka_processada_data
    ON mensagem_kafka_processada (data_processamento);