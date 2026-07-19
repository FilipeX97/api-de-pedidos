package br.com.api.pedidos.shared.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Envelope padrão utilizado nas respostas da API"
)
public record RespostaApi<T>(
        @Schema(
                description = "Indica se a operação foi concluída com sucesso"
        )
        boolean sucesso,

        @Schema(
                description = """
                        Conteúdo retornado pela operação. O formato depende
                        do endpoint que está sendo acessado
                        """,
                nullable = true
        )
        T dados,

        @Schema(
                description = "Mensagem descritiva do resultado"
        )
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
