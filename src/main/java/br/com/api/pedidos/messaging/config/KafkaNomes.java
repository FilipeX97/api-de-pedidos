package br.com.api.pedidos.messaging.config;

public final class KafkaNomes {

    public static final String TOPICO_EVENTOS_PEDIDOS = "api.pedidos.eventos.v1";
    public static final String GRUPO_INTEGRACAO = "api-pedidos-integracao-v1";
    public static final String GRUPO_ANALYTICS = "api-pedidos-analytics-v1";
    public static final int NUMERO_PARTICOES = 3;
    public static final short FATOR_REPLICACAO = 1;

    private KafkaNomes() { }
}
