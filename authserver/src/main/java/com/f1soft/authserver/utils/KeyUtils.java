package com.f1soft.authserver.utils;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class KeyUtils {

    public static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048); // Standard 2048-bit key
        return keyGen.generateKeyPair();
    }

    public static String encodePublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static String encodePrivateKey(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public static PublicKey decodePublicKey(String base64PublicKey) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64PublicKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(new java.security.spec.X509EncodedKeySpec(bytes));
    }

    public static PrivateKey decodePrivateKey(String base64PrivateKey) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64PrivateKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(bytes));
    }

    // --------------------------
    // Main method to generate keys
    // --------------------------
    public static void main(String[] args) throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        String publicKeyBase64 = encodePublicKey(publicKey);
        String privateKeyBase64 = encodePrivateKey(privateKey);

        System.out.println("----- PUBLIC KEY -----");
        System.out.println(publicKeyBase64);
        System.out.println("----- PRIVATE KEY -----");
        System.out.println(privateKeyBase64);

    }
}
