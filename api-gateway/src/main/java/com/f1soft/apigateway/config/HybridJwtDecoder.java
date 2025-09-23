package com.f1soft.apigateway.config;

import com.f1soft.apigateway.util.KeyFileUtils;
import com.f1soft.apigateway.util.KeyUtils;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@Component
public class HybridJwtDecoder implements JwtDecoder {

    private final RSAPrivateKey privateKey;
    private final ConfigurableJWTProcessor<SecurityContext> signedJwtProcessor;

    public HybridJwtDecoder() throws Exception {
        // 1. Load private key for decryption (to unwrap JWE)
        this.privateKey = (RSAPrivateKey) KeyUtils.decodePrivateKey(KeyFileUtils.readKeyFromFile(String.valueOf(Paths.get("/app/keys/client/private.key"))));

        // 2. Load signing public key for signature verification (auth server's public key)
        RSAPublicKey signingPublicKey = (RSAPublicKey) KeyUtils.decodePublicKey(KeyFileUtils.readKeyFromFile(String.valueOf(Paths.get("/app/keys/authserver/public.key"))));

        // 3. Configure JWT Processor for signature verification (RS256)
        // Had to specifically specify the keyId that I made static and also the algorithm.
        this.signedJwtProcessor = new DefaultJWTProcessor<>();
        JWK jwk = new RSAKey.Builder(signingPublicKey).keyID("auth-server-main-key")
                .algorithm(JWSAlgorithm.RS256).build();

        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));

        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);

        signedJwtProcessor.setJWSKeySelector(keySelector);
    }


    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            // 1. Parse and decrypt JWE
            EncryptedJWT jwe = EncryptedJWT.parse(token);
            jwe.decrypt(new RSADecrypter(privateKey));

            // 2. Extract the inner signed JWT (serialized string)
            // This is the crucial fix: get the payload as a signed JWT
            SignedJWT signedJWT = jwe.getPayload().toSignedJWT();
            if (signedJWT == null) {
                throw new JwtException("JWE payload is not a signed JWT");
            }
            String innerToken = signedJWT.serialize();

            // 3. Verify the signature of the inner JWT
            JWTClaimsSet claimsSet = signedJwtProcessor.process(innerToken, null);


            // 4. Build Spring Security Jwt object. Had to convert date claims from java.util.date to Instant.
            Jwt jwt = Jwt.withTokenValue(token).headers(h -> h.putAll(signedJWT.getHeader().toJSONObject())).claims(c -> {
                claimsSet.getClaims().forEach((k, v) -> {
                    if (v instanceof Date) {
                        c.put(k, ((Date) v).toInstant());
                    } else {
                        c.put(k, v);
                    }
                });
            }).build();


            // 5. Optional: print decoded claims
            System.out.println("Decoded JWT claims: " + claimsSet.getClaims());

            return jwt;

        } catch (Exception e) {
            throw new JwtException("Failed to decode JWE+JWS token", e);
        }
    }
}
