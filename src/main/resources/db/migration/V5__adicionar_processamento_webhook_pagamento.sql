ALTER TABLE webhook_pagamento_recebido
    ADD COLUMN status_processamento VARCHAR(30) NOT NULL DEFAULT 'RECEBIDO';

ALTER TABLE webhook_pagamento_recebido
    ADD COLUMN mensagem_erro VARCHAR(2000);

ALTER TABLE webhook_pagamento_recebido
    ADD COLUMN data_processamento TIMESTAMP;

CREATE INDEX idx_webhook_pagamento_status_processamento
    ON webhook_pagamento_recebido (status_processamento);