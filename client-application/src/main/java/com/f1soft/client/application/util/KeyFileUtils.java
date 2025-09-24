package com.f1soft.client.application.util;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;

public class KeyFileUtils {

    public static void saveKeyToFile(String key, String filename) throws IOException {
        Files.writeString(Path.of(filename), key);
        System.out.println("Key saved to: " + filename);
    }

    public static String readKeyFromFile(String filePath) throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            InputStream inputStream = resource.getInputStream();
            // Read the file content
            String fileContent = new String(inputStream.readAllBytes());
            inputStream.close();

            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read private key file", e);
        }
    }

    public static void main(String[] args) throws Exception {
        KeyPair keyPair = KeyUtils.generateRsaKeyPair();

        // Encode keys as Base64
        String publicKey = KeyUtils.encodePublicKey(keyPair.getPublic());
        String privateKey = KeyUtils.encodePrivateKey(keyPair.getPrivate());

        // Save to files
        saveKeyToFile(publicKey, "auth_rsa_public.key");
        saveKeyToFile(privateKey, "auth_rsa_private.key");

        // Optional: Read back from files
        String loadedPublicKey = readKeyFromFile("auth_rsa_public.key");
        String loadedPrivateKey = readKeyFromFile("auth_rsa_private.key");

        System.out.println("Loaded Public Key: " + loadedPublicKey);
        System.out.println("Loaded Private Key: " + loadedPrivateKey);
    }
}
