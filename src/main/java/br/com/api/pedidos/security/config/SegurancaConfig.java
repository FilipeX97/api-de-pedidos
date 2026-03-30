package br.com.api.pedidos.security.config;

import br.com.api.pedidos.security.filter.JwtFiltroAutenticacao;
import br.com.api.pedidos.security.filter.FiltroIntervaloRequisicao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SegurancaConfig {

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/auth/**").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtFiltroAutenticacao,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        filtroIntervaloRequisicao,
                        JwtFiltroAutenticacao.class
                );

        return http.build();
    }

}
