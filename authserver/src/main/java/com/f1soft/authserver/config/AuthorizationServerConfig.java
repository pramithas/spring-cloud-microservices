package com.f1soft.authserver.config;

import com.f1soft.authserver.encoder.JweJwtEncoder;
import com.f1soft.authserver.service.ClientService;
import com.f1soft.authserver.utils.KeyFileUtils;
import com.f1soft.authserver.utils.KeyUtils;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@RequiredArgsConstructor
public class AuthorizationServerConfig {

    private ClientService clientService;
    private final OAuth2TokenGenerator<?> tokenGenerator; // Inject the token generator


    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService() {
        return new InMemoryOAuth2AuthorizationConsentService();
    }

    public RSAPublicKey fetchClientPublicKey(String jwksUrl) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // Fetch JWKS JSON from client
            ResponseEntity<String> response = restTemplate.getForEntity(jwksUrl, String.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Empty JWKS response from client");
            }

            // Parse JWKS
            JWKSet jwkSet = JWKSet.parse(response.getBody());

            // Assuming one key (or pick by 'kid' if multiple)
            JWK jwk = jwkSet.getKeys().get(0);

            if (!(jwk instanceof RSAKey)) {
                throw new RuntimeException("Expected RSA key but got " + jwk.getKeyType());
            }

            return ((RSAKey) jwk).toRSAPublicKey();

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch/parse client public key", e);
        }
    }

    @Bean
    public JweJwtEncoder jwtEncoder() throws Exception {
        System.out.println(" JweJwtEncoder initialized with hybrid RSA+AES (static keys)");

        // 1. Load signing RSA private + public key from static files
        RSAPrivateKey signingPrivateKey = (RSAPrivateKey) KeyUtils.decodePrivateKey(
                KeyFileUtils.readKeyFromFile("../auth_rsa_private.key")
        );
        RSAPublicKey signingPublicKey = (RSAPublicKey) KeyUtils.decodePublicKey(
                KeyFileUtils.readKeyFromFile("../auth_rsa_public.key")
        );


        // 2. Load client RSA public key from static file
        RSAPublicKey clientPublicKey = (RSAPublicKey) KeyUtils.decodePublicKey(
                KeyFileUtils.readKeyFromFile("../rsa_public.key")
        );

        RSAKey clientRsaKey = new RSAKey.Builder(clientPublicKey).build();

        // 3. Build signing JWK (for JWT signature)
        RSAKey signingKey = new RSAKey.Builder(signingPublicKey)
                .privateKey(signingPrivateKey)
                .keyID("auth-server-main-key")
                // explicitly set algorithm to RS256.
                .algorithm(JWSAlgorithm.RS256)
                .build();

        // 4. Wrap the signing key in a JWK source
        JWKSource<SecurityContext> jwkSource =
                new ImmutableJWKSet<>(new JWKSet(signingKey));

        // 5. Return custom encoder with hybrid encryption
        return new JweJwtEncoder(
                jwkSource,
                clientRsaKey,
                JWEAlgorithm.RSA_OAEP_256,   // asymmetric key encryption
                EncryptionMethod.A256GCM     // symmetric content encryption
        );
    }




    /**
     * If the default chain (@Order(2)) ran first, it might intercept /oauth2/token or /oauth2/authorize requests and require authentication
     * incorrectly, breaking the Authorization Server endpoints
     */

    /**
     * This sets up all the default security for an OAuth2 Authorization Server.
     * It configures endpoints like /oauth2/authorize, /oauth2/token, /oauth2/jwks, etc.
     * It also applies login support (formLogin) for users to authenticate when accessing OAuth2 flows.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login**", "/oauth2/**", "/.well-known/**", "/public/**","/debug/encoder-test","/mytoken")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(Customizer.withDefaults()
                );

        return http.build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration google = ClientRegistration.withRegistrationId("google")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .redirectUri("http://localhost:8085/auth/login/oauth2/code/{registrationId}")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .build();
        return new InMemoryClientRegistrationRepository(google);
    }

    /**
     * This is the catch-all chain for any other request that doesn’t match the authorization server endpoints.
     * Example: /hello, /home, /user/profile in your app.
     * It ensures that any request requires authentication, and if the user is not authenticated, Spring Security will redirect them to /login automatically
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        RequestMatcher endpointsMatcher = authorizationServerConfigurer
                .getEndpointsMatcher();

        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/oauth2/jwks").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
//                .oauth2Login(oauth2 -> oauth2.loginPage("/oauth2/authorization/google"))
                .oauth2Login(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .apply(authorizationServerConfigurer)
                .tokenGenerator(tokenGenerator); // Use injected generator

        return http.build();
    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("testuser")
                .password("{noop}password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }


    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8081")
                .jwkSetEndpoint("/.well-known/jwks.json")
                .build();
    }
}
