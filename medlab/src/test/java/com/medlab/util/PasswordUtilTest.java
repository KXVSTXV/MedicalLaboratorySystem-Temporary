package com.medlab.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PasswordUtil SHA-256 hashing.
 */
class PasswordUtilTest {

    @Test
    void hash_producesConsistentOutput() {
        String hash1 = PasswordUtil.hash("Admin@123");
        String hash2 = PasswordUtil.hash("Admin@123");
        assertEquals(hash1, hash2, "Same input should produce same hash");
    }

    @Test
    void hash_differentInputsDifferentOutput() {
        assertNotEquals(PasswordUtil.hash("password1"), PasswordUtil.hash("password2"));
    }

    @Test
    void hash_producesHex64Chars() {
        // SHA-256 always produces a 64-character hex string
        assertEquals(64, PasswordUtil.hash("any string").length());
    }

    @Test
    void verify_correctPassword_returnsTrue() {
        String hash = PasswordUtil.hash("SecurePass@123");
        assertTrue(PasswordUtil.verify("SecurePass@123", hash));
    }

    @Test
    void verify_incorrectPassword_returnsFalse() {
        String hash = PasswordUtil.hash("SecurePass@123");
        assertFalse(PasswordUtil.verify("WrongPass", hash));
    }

    @Test
    void verify_emptyPassword() {
        String hash = PasswordUtil.hash("");
        assertTrue(PasswordUtil.verify("", hash));
        assertFalse(PasswordUtil.verify("notempty", hash));
    }

    @Test
    void verify_caseInsensitiveHashComparison() {
        String hash = PasswordUtil.hash("Test@123").toUpperCase();
        assertTrue(PasswordUtil.verify("Test@123", hash),
            "verify() should be case-insensitive for the hash string");
    }
}
