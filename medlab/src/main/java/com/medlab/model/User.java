package com.medlab.model;

/**
 * POJO representing a system User for authentication / authorization.
 *
 * Roles:
 *   ADMIN            — full access
 *   RECEPTIONIST     — can manage patients and test orders
 *   LAB_TECHNICIAN   — can handle samples and reports
 *
 * Future: becomes a JPA @Entity with Spring Security UserDetails.
 */
public class User {

    public enum Role {
        ADMIN,
        RECEPTIONIST,
        LAB_TECHNICIAN
    }

    private int    id;
    private String username;
    private String email;
    private String mobileNumber;
    private String passwordHash;
    private Role   role;
    private boolean isActive;
    private boolean isDeleted;
    private String createdAt;
    private String updatedAt;
    private String createdBy;

    // ── Constructors ──────────────────────────────────────────────

    public User() {}

    public User(String username, String email, String mobileNumber,
                String passwordHash, Role role, String createdBy) {
        this.username     = username;
        this.email        = email;
        this.mobileNumber = mobileNumber;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.isActive     = true;
        this.isDeleted    = false;
        this.createdBy    = createdBy;
    }

    public User(int id, String username, String email, String mobileNumber,
                String passwordHash, String role, boolean isActive,
                boolean isDeleted, String createdAt, String updatedAt, String createdBy) {
        this.id           = id;
        this.username     = username;
        this.email        = email;
        this.mobileNumber = mobileNumber;
        this.passwordHash = passwordHash;
        this.role         = Role.valueOf(role);
        this.isActive     = isActive;
        this.isDeleted    = isDeleted;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
        this.createdBy    = createdBy;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int     getId()                         { return id; }
    public void    setId(int id)                   { this.id = id; }

    public String  getUsername()                   { return username; }
    public void    setUsername(String u)           { this.username = u; }

    public String  getEmail()                      { return email; }
    public void    setEmail(String e)              { this.email = e; }

    public String  getMobileNumber()               { return mobileNumber; }
    public void    setMobileNumber(String m)       { this.mobileNumber = m; }

    public String  getPasswordHash()               { return passwordHash; }
    public void    setPasswordHash(String h)       { this.passwordHash = h; }

    public Role    getRole()                       { return role; }
    public void    setRole(Role r)                 { this.role = r; }

    public boolean isActive()                      { return isActive; }
    public void    setActive(boolean a)            { this.isActive = a; }

    public boolean isDeleted()                     { return isDeleted; }
    public void    setDeleted(boolean d)           { this.isDeleted = d; }

    public String  getCreatedAt()                  { return createdAt; }
    public void    setCreatedAt(String s)          { this.createdAt = s; }

    public String  getUpdatedAt()                  { return updatedAt; }
    public void    setUpdatedAt(String s)          { this.updatedAt = s; }

    public String  getCreatedBy()                  { return createdBy; }
    public void    setCreatedBy(String s)          { this.createdBy = s; }

    // ── toString ──────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "User{id=%d, username='%s', email='%s', role=%s, active=%b}",
            id, username, email, role, isActive);
    }
}
