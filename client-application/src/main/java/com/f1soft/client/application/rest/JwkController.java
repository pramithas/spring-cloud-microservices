package com.f1soft.client.application.rest;

import com.f1soft.client.application.util.KeyFileUtils;
import com.f1soft.client.application.util.KeyUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.EncryptedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
public class JwkController {

    private final JwtDecoder jweJwtDecoder;

    public JwkController(@Qualifier("jweJwtDecoder") JwtDecoder jweJwtDecoder) {
        this.jweJwtDecoder = jweJwtDecoder;
    }

    @GetMapping("/debug-token")
    public Map<String, Object> debugToken(@RequestHeader("Myheader") String authHeader) {
        try {
            System.out.println("Received header: " + authHeader);
            String token = authHeader.replace("Bearer ", "").trim();

            // 1. Load private key from file
            RSAPrivateKey privateKey = (RSAPrivateKey) KeyUtils.decodePrivateKey(
                    KeyFileUtils.readKeyFromFile(String.valueOf(Paths.get("/app/keys/client/private.key")))
            );

            // 2. Load public key (optional, for completeness)
            RSAPublicKey publicKey = (RSAPublicKey) KeyUtils.decodePublicKey(
                    KeyFileUtils.readKeyFromFile(String.valueOf(Paths.get("/app/keys/client/public.key")))
            );

            // 3. Build RSAKey
            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("client-key-1")
                    .build();

            // 4. Parse and decrypt the JWE
            EncryptedJWT jwe = EncryptedJWT.parse(token);
            jwe.decrypt(new RSADecrypter(rsaKey.toRSAPrivateKey()));

            // 5. Extract claims
            Map<String, Object> claims = jwe.getJWTClaimsSet().getClaims();

            // 6. Print claims for debugging
            System.out.println("Decrypted JWT claims:");
            claims.forEach((k, v) -> System.out.println(k + " : " + v));

            return claims;

        } catch (Exception e) {
            System.err.println("Failed to decode or decrypt JWT: " + e.getMessage());
            e.printStackTrace();
            return Map.of(
                    "error", "Failed to decode or decrypt JWT",
                    "message", e.getMessage()
            );
        }
    }


}
