package com.f1soft.client.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String GATEWAY_HEADER = "X-GATEWAY-AUTH";
    private static final String GATEWAY_SECRET = "some-secret";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().access((authentication, context) -> {
                            String header = context.getRequest().getHeader(GATEWAY_HEADER);
                            boolean allowed = GATEWAY_SECRET.equals(header);
                            return new AuthorizationDecision(allowed);
                        })
                )
                .formLogin(formLogin -> formLogin.disable()) // Disable form login
                .httpBasic(httpBasic -> httpBasic.disable()) // Disable HTTP Basic
                .csrf(csrf -> csrf.disable()); // Disable CSRF

        return http.build();
    }
}
