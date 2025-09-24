package com.f1soft.apigateway.util;

public class KeyPathUtils {

    /**
     * Determine the path of the key based on appName, keyType, and environment.
     *
     * @param appName either "authserver" or "client"
     * @param keyType either "private" or "public"
     * @return full path to the key file
     */
    public static String getKeyPath(String appName, String keyType) {
        if (!keyType.equalsIgnoreCase("private") && !keyType.equalsIgnoreCase("public")) {
            throw new IllegalArgumentException("keyType must be 'private' or 'public'");
        }

        // Detect if running inside Kubernetes
        boolean isLocal = System.getenv("KUBERNETES_SERVICE_HOST") == null;

        if (isLocal) {
            // Default local path
            switch (appName.toLowerCase()) {
                case "authserver":
                    return "keys/authserver/" + keyType + ".key";
                case "client":
                    return "keys/client/" + keyType + ".key";
                default:
                    throw new IllegalArgumentException("Unknown appName: " + appName);
            }
        } else {
            // Kubernetes path
            return "/app/keys/" + appName + "/" + keyType + ".key";
        }
    }

    // Optional helper methods for convenience
    public static String getAuthServerPrivateKey() {
        return getKeyPath("authserver", "private");
    }

    public static String getAuthServerPublicKey() {
        return getKeyPath("authserver", "public");
    }

    public static String getClientPrivateKey() {
        return getKeyPath("client", "private");
    }

    public static String getClientPublicKey() {
        return getKeyPath("client", "public");
    }
}
