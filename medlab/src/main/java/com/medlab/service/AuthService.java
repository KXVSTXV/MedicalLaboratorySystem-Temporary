package com.medlab.service;

import com.medlab.auth.AuthContext;
import com.medlab.model.User;
import com.medlab.repository.UserRepository;
import com.medlab.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Authentication & Authorization Service.
 *
 * Handles login, registration, and user management.
 * Populates AuthContext with the current user session.
 *
 * Future (Spring Boot v2): Replaced by Spring Security with JWT tokens.
 */
public class AuthService {

    private final UserRepository userRepo;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * Authenticates a user. Sets session in AuthContext on success.
     *
     * @return the authenticated User
     * @throws SecurityException    if credentials are wrong
     * @throws IllegalStateException if account is inactive
     */
    public User login(String username, String password) throws SQLException {
        Optional<User> userOpt = userRepo.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new SecurityException("Invalid username or password.");
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            throw new IllegalStateException("Account is deactivated. Contact an administrator.");
        }

        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new SecurityException("Invalid username or password.");
        }

        AuthContext.setCurrentUser(user);
        System.out.println("  ✔ Logged in as: " + user.getUsername() + " [" + user.getRole() + "]");
        return user;
    }

    /**
     * Registers a new user (ADMIN only).
     */
    public User registerUser(String username, String email, String mobileNumber,
                             String password, User.Role role) throws SQLException {
        AuthContext.requireRole(User.Role.ADMIN);

        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }

        String hash = PasswordUtil.hash(password);
        User user = new User(username, email, mobileNumber, hash, role,
                             AuthContext.getCurrentUsername());
        return userRepo.save(user);
    }

    public void logout() {
        String who = AuthContext.getCurrentUsername();
        AuthContext.logout();
        System.out.println("  ✔ " + who + " has been logged out.");
    }

    public List<User> getAllUsers() throws SQLException {
        AuthContext.requireRole(User.Role.ADMIN);
        return userRepo.findAll();
    }

    public void softDeleteUser(int userId) throws SQLException {
        AuthContext.requireRole(User.Role.ADMIN);
        userRepo.softDelete(userId);
    }
}
