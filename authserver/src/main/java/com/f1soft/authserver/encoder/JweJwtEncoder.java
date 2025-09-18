package com.f1soft.authserver.encoder;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class JweJwtEncoder implements JwtEncoder {

    private final NimbusJwtEncoder delegate;
    private final RSAKey encryptionKey;
    private final JWEAlgorithm jweAlgorithm;
    private final EncryptionMethod encryptionMethod;

    public JweJwtEncoder(JWKSource<SecurityContext> jwkSource,
                         RSAKey encryptionKey,
                         JWEAlgorithm jweAlgorithm,
                         EncryptionMethod encryptionMethod) {
        this.delegate = new NimbusJwtEncoder(jwkSource);
        this.encryptionKey = encryptionKey;
        this.jweAlgorithm = jweAlgorithm;
        this.encryptionMethod = encryptionMethod;

        System.out.printf(
                "JweJwtEncoder initialized with keyId=%s, alg=%s, enc=%s%n",
                encryptionKey.getKeyID(), jweAlgorithm.getName(), encryptionMethod.getName()
        );
    }

    @Override
    public Jwt encode(JwtEncoderParameters parameters) throws JwtEncodingException {
        // Step 1: Generate signed JWT (JWS)
        Jwt signedJwt = delegate.encode(parameters);

        String jwsString = signedJwt.getTokenValue(); // <-- actual signed JWS compact string

        try {
            // Step 2: Parse the JWS string into a SignedJWT
            SignedJWT signedNimbusJwt = SignedJWT.parse(jwsString);

            // Step 3: Build JWE header with alg/enc
            JWEHeader header = new JWEHeader.Builder(jweAlgorithm, encryptionMethod)
                    .contentType("JWT") // important: nested JWT
                    .build();

            // Step 4: Create EncryptedJWT with the signed JWS payload
            JWEObject jweObject = new JWEObject(header, new Payload(signedNimbusJwt));

            // Step 5: Encrypt with client’s RSA public key
            jweObject.encrypt(new RSAEncrypter(encryptionKey.toRSAPublicKey()));

            // Step 6: Return Spring Jwt object
            return Jwt.withTokenValue(jweObject.serialize())
                    .headers(h -> h.putAll(signedJwt.getHeaders())) // preserve alg, kid, etc.
                    .claims(c -> c.putAll(signedJwt.getClaims()))   // keep claims for Spring
                    .build();

        } catch (Exception e) {
            throw new JwtEncodingException("Failed to encrypt JWT", e);
        }
    }

}
