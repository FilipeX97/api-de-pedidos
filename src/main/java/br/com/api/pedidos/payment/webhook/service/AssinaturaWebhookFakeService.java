package br.com.api.pedidos.payment.webhook.service;

import br.com.api.pedidos.payment.webhook.config.WebhookFakeProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class AssinaturaWebhookFakeService {

    private static final String ALGORITMO = "HmacSHA256";

    private final WebhookFakeProperties properties;

    public AssinaturaWebhookFakeService(WebhookFakeProperties properties) {
        this.properties = properties;
    }

    public void validarAssinatura(
            String corpoOriginal,
            String assinaturaRecebida
    ) {
        if (assinaturaRecebida == null || assinaturaRecebida.isBlank()) {
            throw new SecurityException("Assinatura do webhook não enviada");
        }

        String assinaturaEsperada = gerarAssinatura(corpoOriginal);

        boolean assinaturaValida = MessageDigest.isEqual(
                assinaturaEsperada.getBytes(StandardCharsets.UTF_8),
                assinaturaRecebida.getBytes(StandardCharsets.UTF_8)
        );

        if (!assinaturaValida) {
            throw new SecurityException("Assinatura do webhook inválida");
        }
    }

    public String gerarAssinatura(String corpoOriginal) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);

            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    properties.secret().getBytes(StandardCharsets.UTF_8),
                    ALGORITMO
            );

            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(
                    corpoOriginal.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Erro ao gerar assinatura do webhook",
                    e
            );
        }
    }
}
