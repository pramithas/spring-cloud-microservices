package com.f1soft.client.application.rest;

import com.f1soft.client.application.config.OAuth2ClientProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
public class OAuthCallbackController {

    private final OAuth2ClientProperties properties;

    public OAuthCallbackController(OAuth2ClientProperties properties) {
        this.properties = properties;
    }

//    @GetMapping("/oauth2/callback")
//    public String oauthCallback(@RequestParam String code) {
//
//        System.out.println("Client ID: " + properties.getId());
//        System.out.println("Redirect URI: " + properties.getRedirectUri());
//        System.out.println("Token URL: " + properties.getTokenUrl());
//        System.out.println("Authorization Code: " + code);
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        String auth = properties.getId() + ":" + properties.getSecret();
//        System.out.println("auth: " + auth);
//        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
//        headers.set("Authorization", "Basic " + encodedAuth);
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("grant_type", "authorization_code");
//        body.add("code", code);
//        body.add("redirect_uri", properties.getRedirectUri());
//        System.out.println("body: " + body);
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
//        ResponseEntity<String> response = restTemplate.postForEntity(properties.getTokenUrl(), request, String.class);
//
//        String responseString =  response.getBody();
//        System.out.println("The response token is"+ responseString);
//        return responseString;
//    }

    @GetMapping("/oauth2/pkce/callback")
    public String pkceCallback(@RequestParam String code) {
        String codeVerifier = "ZYp9SZYmjKbd7HZ0VcWP5vnlRBgFaUtw6JYl7FLSOqE";

        /**
         * The authorization server stores the code challenge that is sent during /authorize endpoint call.
         * And, when we send code verifier during /token call, it re-calculates the code challenge from the
         * code verifier. If they match, the request is valid.
         */

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", properties.getRedirectUri());
        body.add("code_verifier", codeVerifier);
        body.add("client_id", properties.getId());

        System.out.println("body: " + body);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(properties.getTokenUrl(), request, String.class);
        return response.getBody();
    }

//    @GetMapping("/oauth2/client-credentials")
//    public ResponseEntity<Map<String, Object>> clientCredentialsFlowExample() {
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = createBasicAuthHeaders(properties.getId(), properties.getSecret());
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("grant_type", "client_credentials");
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
//        ResponseEntity<Map> response = restTemplate.postForEntity(properties.getTokenUrl(), request, Map.class);
//
//        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
//    }

//    @GetMapping("/oauth2/refresh-token")
//    public ResponseEntity<Map<String, Object>> refreshAccessToken(@RequestParam String refreshToken) {
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = createBasicAuthHeaders(properties.getId(), properties.getSecret());
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("grant_type", "refresh_token");
//        body.add("refresh_token", refreshToken);
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
//        ResponseEntity<Map> response = restTemplate.postForEntity(properties.getTokenUrl(), request, Map.class);
//
//        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
//    }

    private HttpHeaders createBasicAuthHeaders(String clientId, String clientSecret) {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }
}
