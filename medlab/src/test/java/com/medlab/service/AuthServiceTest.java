package com.medlab.service;

import com.medlab.auth.AuthContext;
import com.medlab.model.User;
import com.medlab.repository.UserRepository;
import com.medlab.util.PasswordUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepo;
    @InjectMocks private AuthService authService;

    private static final String PLAIN_PASSWORD = "Admin@123";
    private static final String HASHED_PASSWORD = PasswordUtil.hash(PLAIN_PASSWORD);

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new User(1, "admin", "admin@medlab.com", "9999999999",
                             HASHED_PASSWORD, "ADMIN", true, false,
                             "2024-01-01", "2024-01-01", "SYSTEM");
    }

    @AfterEach
    void tearDown() {
        AuthContext.logout();
    }

    // ── login ─────────────────────────────────────────────────────

    @Test
    @DisplayName("login: valid credentials sets AuthContext")
    void login_success() throws SQLException {
        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        User result = authService.login("admin", PLAIN_PASSWORD);

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals(User.Role.ADMIN, result.getRole());
        assertTrue(AuthContext.isLoggedIn());
        assertEquals("admin", AuthContext.getCurrentUsername());
    }

    @Test
    @DisplayName("login: wrong password throws SecurityException")
    void login_wrongPassword() throws SQLException {
        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        assertThrows(SecurityException.class,
            () -> authService.login("admin", "wrongpassword"));
        assertFalse(AuthContext.isLoggedIn());
    }

    @Test
    @DisplayName("login: unknown username throws SecurityException")
    void login_unknownUser() throws SQLException {
        when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(SecurityException.class,
            () -> authService.login("ghost", PLAIN_PASSWORD));
    }

    @Test
    @DisplayName("login: inactive account throws IllegalStateException")
    void login_inactiveAccount() throws SQLException {
        User inactive = new User(2, "inactive", "i@medlab.com", "1111111111",
                                 HASHED_PASSWORD, "RECEPTIONIST", false, false,
                                 "2024-01-01", "2024-01-01", "SYSTEM");
        when(userRepo.findByUsername("inactive")).thenReturn(Optional.of(inactive));

        assertThrows(IllegalStateException.class,
            () -> authService.login("inactive", PLAIN_PASSWORD));
    }

    // ── registerUser ──────────────────────────────────────────────

    @Test
    @DisplayName("registerUser: ADMIN can register new users")
    void registerUser_adminSuccess() throws SQLException {
        AuthContext.setCurrentUser(adminUser);
        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(userRepo.existsByEmail("new@medlab.com")).thenReturn(false);
        when(userRepo.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99);
            return u;
        });

        User created = authService.registerUser("newuser", "new@medlab.com",
                                                 "5555555555", "Pass@123",
                                                 User.Role.RECEPTIONIST);

        assertNotNull(created);
        assertEquals(99, created.getId());
        assertEquals(User.Role.RECEPTIONIST, created.getRole());
    }

    @Test
    @DisplayName("registerUser: non-ADMIN throws SecurityException")
    void registerUser_nonAdminForbidden() throws SQLException {
        User receptionist = new User(3, "rec1", "r@medlab.com", "8888888888",
                                     HASHED_PASSWORD, "RECEPTIONIST", true, false,
                                     "2024-01-01", "2024-01-01", "SYSTEM");
        AuthContext.setCurrentUser(receptionist);

        assertThrows(SecurityException.class,
            () -> authService.registerUser("x", "x@x.com", "1234567890",
                                           "pass", User.Role.LAB_TECHNICIAN));
    }

    @Test
    @DisplayName("registerUser: duplicate username throws IllegalArgumentException")
    void registerUser_duplicateUsername() throws SQLException {
        AuthContext.setCurrentUser(adminUser);
        when(userRepo.existsByUsername("admin")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> authService.registerUser("admin", "other@medlab.com",
                                           "1234567890", "pass", User.Role.RECEPTIONIST));
    }

    // ── logout ────────────────────────────────────────────────────

    @Test
    @DisplayName("logout: clears AuthContext")
    void logout_clearsSession() throws SQLException {
        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        authService.login("admin", PLAIN_PASSWORD);
        assertTrue(AuthContext.isLoggedIn());

        authService.logout();
        assertFalse(AuthContext.isLoggedIn());
    }

    // ── getAllUsers ───────────────────────────────────────────────

    @Test
    @DisplayName("getAllUsers: returns list for ADMIN")
    void getAllUsers_admin() throws SQLException {
        AuthContext.setCurrentUser(adminUser);
        when(userRepo.findAll()).thenReturn(List.of(adminUser));

        List<User> users = authService.getAllUsers();
        assertEquals(1, users.size());
    }

    @Test
    @DisplayName("getAllUsers: non-ADMIN throws SecurityException")
    void getAllUsers_nonAdmin() throws SQLException {
        User receptionist = new User(3, "rec1", "r@medlab.com", "8888888888",
                                     HASHED_PASSWORD, "RECEPTIONIST", true, false,
                                     "2024-01-01", "2024-01-01", "SYSTEM");
        AuthContext.setCurrentUser(receptionist);

        assertThrows(SecurityException.class, () -> authService.getAllUsers());
    }
}
