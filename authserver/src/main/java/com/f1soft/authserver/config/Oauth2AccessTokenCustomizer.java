package com.f1soft.authserver.config;

import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

public class Oauth2AccessTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {
    @Override
    public void customize(JwtEncodingContext context) {
            System.out.println("The token type is: " +context.getTokenType().getValue());
            if (context.getTokenType().getValue().equals("access_token")) {
                // Add custom claims to your JWT tokens
                context.getClaims().claims(claims -> {
                    claims.put("iss", "http://localhost:8081");
                    claims.put("custom_claim", "your_custom_value");
                    // You can add more custom claims based on user info
                });
            }
    }
}
