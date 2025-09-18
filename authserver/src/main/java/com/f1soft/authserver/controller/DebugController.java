package com.f1soft.authserver.controller;

import com.f1soft.authserver.encoder.JweJwtEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    private final JweJwtEncoder jweJwtEncoder;

    public DebugController(JweJwtEncoder jweJwtEncoder) {
        this.jweJwtEncoder = jweJwtEncoder;
    }

    @GetMapping("/debug/encoder-test")
    public String testEncoder() {
        try {
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("http://localhost:8081")
                    .subject("test-user")
                    .claim("test", "value")
                    .build();

            Jwt jwt = jweJwtEncoder.encode(JwtEncoderParameters.from(claims));

            String token = jwt.getTokenValue();
            int parts = token.split("\\.").length;

            return "Token parts: " + parts + " - " +
                    (parts == 5 ? "JWE (Encrypted) ✓" : "JWT (Not encrypted) ✗") +
                    "\n\nToken:\n" + token;
        } catch (Exception e) {
            return "Encoder Error: " + e.getMessage();
        }
    }
}
