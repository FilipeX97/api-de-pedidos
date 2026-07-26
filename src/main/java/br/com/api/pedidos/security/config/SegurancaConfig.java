package br.com.api.pedidos.security.config;

import br.com.api.pedidos.security.filter.JwtFiltroAutenticacao;
import br.com.api.pedidos.security.filter.FiltroIntervaloRequisicao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SegurancaConfig {

    private static final String[] ROTAS_SWAGGER = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**"
    };

    private static final String[] ROTAS_ACTUATOR_PUBLICAS = {
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info"
    };

    private final JwtFiltroAutenticacao jwtFiltroAutenticacao;
    private final FiltroIntervaloRequisicao filtroIntervaloRequisicao;

    public SegurancaConfig(JwtFiltroAutenticacao jwtFiltroAutenticacao,
                           FiltroIntervaloRequisicao filtroIntervaloRequisicao) {
        this.jwtFiltroAutenticacao = jwtFiltroAutenticacao;
        this.filtroIntervaloRequisicao = filtroIntervaloRequisicao;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable
                )
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(ROTAS_SWAGGER).permitAll()
                                .requestMatchers(ROTAS_ACTUATOR_PUBLICAS).permitAll()
                                .requestMatchers(
                                        "/actuator/metrics",
                                        "/actuator/metrics/**"
                                ).hasRole("ADMIN")
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/auth/login",
                                        "/auth/refresh",
                                        "/auth/registrar").permitAll()
                                .requestMatchers("/webhooks/**").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .addFilterBefore(
                        filtroIntervaloRequisicao,
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        jwtFiltroAutenticacao,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
