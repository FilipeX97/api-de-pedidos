package br.com.api.pedidos.observability.info;

import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class InformacoesAplicacaoContributor implements InfoContributor {

    private final Environment environment;

    public InformacoesAplicacaoContributor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void contribute(Info.Builder builder) {
        String[] perfisAtivos = environment.getActiveProfiles();

        if(perfisAtivos.length == 0) {
            perfisAtivos = environment.getDefaultProfiles();
        }

        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("ambiente", Arrays.asList(perfisAtivos));
        runtime.put("java", System.getProperty("java.version"));
        runtime.put("springBoot", Objects.requireNonNullElse(SpringBootVersion.getVersion(), "Versão não encontrada"));
        builder.withDetail("runtime", runtime);
    }

}
