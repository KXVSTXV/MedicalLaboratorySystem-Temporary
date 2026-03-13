package com.medlab.auth;

import com.medlab.model.User;

/**
 * Thread-local session context holding the currently authenticated user.
 *
 * Future (Spring Boot v2): Replaced by Spring Security's SecurityContextHolder.
 */
public class AuthContext {

    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    private AuthContext() {}

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static String getCurrentUsername() {
        User u = currentUser.get();
        return u != null ? u.getUsername() : "SYSTEM";
    }

    public static boolean isLoggedIn() {
        return currentUser.get() != null;
    }

    public static void logout() {
        currentUser.remove();
    }

    /**
     * Throws SecurityException if the current user does not have one of the allowed roles.
     */
    public static void requireRole(User.Role... allowedRoles) {
        User u = currentUser.get();
        if (u == null) {
            throw new SecurityException("Not authenticated. Please login first.");
        }
        for (User.Role role : allowedRoles) {
            if (u.getRole() == role) return;
        }
        throw new SecurityException(
            "Access denied. Required role: " + java.util.Arrays.toString(allowedRoles)
            + ", but you are: " + u.getRole());
    }
}
