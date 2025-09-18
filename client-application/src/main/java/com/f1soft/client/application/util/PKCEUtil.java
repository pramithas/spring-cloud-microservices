package com.f1soft.client.application.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PKCEUtil {

    // Generate a random code verifier
    public static String generateCodeVerifier() {
        byte[] code = new byte[32];
        new SecureRandom().nextBytes(code);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code);
    }

    // Generate code challenge from code verifier
    public static String generateCodeChallenge(String codeVerifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    public static void main(String[] args) throws Exception {
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);

        System.out.println("Code Verifier: " + codeVerifier);
        System.out.println("Code Challenge: " + codeChallenge);
    }
}
