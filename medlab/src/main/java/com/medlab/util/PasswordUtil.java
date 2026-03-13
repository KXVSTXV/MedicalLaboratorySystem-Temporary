package com.medlab.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Password hashing utility using SHA-256.
 *
 * NOTE: In production / Spring Boot v2, switch to BCrypt via Spring Security.
 * SHA-256 is used here to avoid external dependencies in the Core Java CLI version.
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Returns the SHA-256 hex digest of the given plain-text password.
     */
    public static String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available on this JVM", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored hash.
     */
    public static boolean verify(String plainText, String storedHash) {
        return hash(plainText).equalsIgnoreCase(storedHash);
    }
}
