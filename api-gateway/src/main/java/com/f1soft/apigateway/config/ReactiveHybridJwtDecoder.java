package com.f1soft.apigateway.config;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

public class ReactiveHybridJwtDecoder implements ReactiveJwtDecoder {
    
    private final HybridJwtDecoder hybridJwtDecoder;
    
    public ReactiveHybridJwtDecoder(HybridJwtDecoder hybridJwtDecoder) {
        this.hybridJwtDecoder = hybridJwtDecoder;
    }
    
    @Override
    public Mono<Jwt> decode(String token) {
        return Mono.fromCallable(() -> hybridJwtDecoder.decode(token))
                .onErrorMap(Exception.class, e -> new JwtException("Failed to decode token", e));
    }
}