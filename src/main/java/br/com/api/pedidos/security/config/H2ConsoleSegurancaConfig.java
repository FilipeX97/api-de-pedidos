package br.com.api.pedidos.security.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("dev")
public class H2ConsoleSegurancaConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain h2ConsoleSeguranca(HttpSecurity http) throws Exception {
        var h2ConsoleRequestMatcher = PathRequest.toH2Console();

        http
                .securityMatcher(h2ConsoleRequestMatcher)
                .authorizeHttpRequests(
                        auth -> auth.anyRequest().permitAll())
                .csrf(csrf ->
                        csrf.ignoringRequestMatchers(h2ConsoleRequestMatcher))
                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }

}
