package br.com.api.pedidos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI apiPedidosOpenAPI() {
        return new OpenAPI()
                .info(criarInformacoesDaApi())
                .components(criarComponentesDeSeguranca());
    }

    @Bean
    public OpenApiCustomizer adicionarSegurancaAosEndpointsProtegidos() {
        return openApi -> openApi.getPaths().forEach(
                (caminho, pathItem) -> {
                    if(rotaPublica(caminho)) {
                        return;
                    }

                    pathItem.readOperations().forEach(
                            operacao -> operacao.addSecurityItem(
                                    new SecurityRequirement()
                                            .addList(BEARER_AUTH)
                            )
                    );
                }
        );
    }

    private Info criarInformacoesDaApi() {
        return new Info()
                .title("API de Pedidos")
                .version("1.0.0")
                .description(
                        """
                        API REST para gerenciamento de usuários, produtos,
                        cupons, pedidos, pagamentos e notificações.
                                
                        Os endpoints protegidos utilizam autenticação JWT
                        por meio do padrão Bearer Token.
                        """
                );
    }

    private Components criarComponentesDeSeguranca() {
        SecurityScheme bearerAuth = new SecurityScheme()
                .name(BEARER_AUTH)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description(
                        "Informe somente o access token JWT, sem escrever o prefixo Bearer."
                );

        return new Components()
                .addSecuritySchemes(BEARER_AUTH, bearerAuth);
    }

    private boolean rotaPublica(String caminho) {
        return caminho.equals("/auth/login")
                || caminho.equals("/auth/refresh")
                || caminho.equals("/auth/registrar")
                || caminho.startsWith("/webhooks/");
    }
}
