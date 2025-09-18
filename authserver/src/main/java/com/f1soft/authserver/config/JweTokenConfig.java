package com.f1soft.authserver.config;

import com.f1soft.authserver.encoder.JweJwtEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.server.authorization.token.*;

@Configuration
public class JweTokenConfig {

    @Bean
    @Primary
    public OAuth2TokenGenerator<?> tokenGenerator(@Lazy JweJwtEncoder jweJwtEncoder) {
        System.out.println("Creating JWE Token Generator with encoder: " + jweJwtEncoder.getClass());

        JwtGenerator jwtGenerator = new JwtGenerator(jweJwtEncoder);
        jwtGenerator.setJwtCustomizer(new Oauth2AccessTokenCustomizer());

        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        
        return new DelegatingOAuth2TokenGenerator(
            jwtGenerator,
            accessTokenGenerator,
            refreshTokenGenerator
        );
    }
}