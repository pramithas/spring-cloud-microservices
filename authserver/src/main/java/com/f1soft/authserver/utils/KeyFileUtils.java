package com.f1soft.authserver.utils;

import com.nimbusds.jose.util.KeyUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileNotFoundException;
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
            // Use relative path from project root
            File file = new File("app/keys/" + filePath);

            if (!file.exists()) {
                throw new FileNotFoundException("Key file not found: " + file.getAbsolutePath());
            }
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read key file: " + filePath, e);
        }
    }

    public static void main(String[] args) throws Exception {
        KeyPair keyPair = AuthServerKeyUtils.generateRsaKeyPair();

        // Encode keys as Base64
        String publicKey = AuthServerKeyUtils.encodePublicKey(keyPair.getPublic());
        String privateKey = AuthServerKeyUtils.encodePrivateKey(keyPair.getPrivate());

        // Save to files
        saveKeyToFile(publicKey, "public.key");
        saveKeyToFile(privateKey, "private.key");

        // Optional: Read back from files
        String loadedPublicKey = readKeyFromFile("public.key");
        String loadedPrivateKey = readKeyFromFile("private.key");

        System.out.println("Loaded Public Key: " + loadedPublicKey);
        System.out.println("Loaded Private Key: " + loadedPrivateKey);
    }
}
