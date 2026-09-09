CREATE TABLE mensagem_processada
(
    id_mensagem       UUID PRIMARY KEY,
    tipo_mensagem     VARCHAR(100) NOT NULL,
    data_processamento TIMESTAMP    NOT NULL
);