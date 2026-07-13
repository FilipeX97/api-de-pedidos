package br.com.api.pedidos.shared.response;

public record RespostaApi<T>(
        boolean sucesso,
        T dados,
        String mensagem) {
    public static <T> RespostaApi<T> sucesso(T dados, String mensagem) {
        return new RespostaApi<>(true, dados, mensagem);
    }

    public static <T> RespostaApi<T> erro(String mensagem) {
        return new RespostaApi<>(false, null, mensagem);
    }

    public static <T> RespostaApi<T> erro(T dados, String mensagem) {
        return new RespostaApi<>(false, dados, mensagem);
    }
}
