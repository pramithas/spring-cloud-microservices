package com.f1soft.client.application.config;

import com.f1soft.client.application.util.KeyFileUtils;
import com.f1soft.client.application.util.KeyUtils;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JweConfig {

    @Bean
    public JwtDecoder jweJwtDecoder() throws Exception {
        System.out.println("jwtDecoder called");

        // 1. Load private key from local file
        RSAPublicKey publicKey = (RSAPublicKey) KeyUtils.decodePublicKey(KeyFileUtils.readKeyFromFile("rsa_public.key"));
        RSAPrivateKey privateKey = (RSAPrivateKey) KeyUtils.decodePrivateKey(KeyFileUtils.readKeyFromFile("rsa_private.key"));

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("client-key-1")
                .build();

        // 3. Wrap the RSAKey in a JWKSet
        JWKSet jwkSet = new JWKSet(rsaKey);
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);

        // 4. Create processor for encrypted JWTs
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        // 5. Configure decryption key selector
        JWEDecryptionKeySelector<SecurityContext > keySelector =
                new JWEDecryptionKeySelector<>(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM, jwkSource);

        jwtProcessor.setJWEKeySelector(keySelector);

        return new NimbusJwtDecoder(jwtProcessor);
    }
}
