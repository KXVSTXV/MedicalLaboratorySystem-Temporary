package com.medlab.model;

/**
 * POJO representing a Patient.
 *
 * Includes:
 *   - Unique fields: email, mobile_number
 *   - Audit fields: created_at, updated_at, created_by
 *   - Soft delete: is_deleted
 *
 * Future: becomes a JPA @Entity with @Table(name="patients")
 */
public class Patient {

    private int     id;
    private String  name;
    private int     age;
    private String  gender;       // M | F | O
    private String  email;
    private String  mobileNumber;
    private String  address;
    private boolean isDeleted;
    private String  createdAt;
    private String  updatedAt;
    private String  createdBy;

    // ── Constructors ──────────────────────────────────────────────

    public Patient() {}

    public Patient(String name, int age, String gender, String email,
                   String mobileNumber, String address, String createdBy) {
        this.name         = name;
        this.age          = age;
        this.gender       = gender;
        this.email        = email;
        this.mobileNumber = mobileNumber;
        this.address      = address;
        this.isDeleted    = false;
        this.createdBy    = createdBy;
    }

    public Patient(int id, String name, int age, String gender, String email,
                   String mobileNumber, String address, boolean isDeleted,
                   String createdAt, String updatedAt, String createdBy) {
        this.id           = id;
        this.name         = name;
        this.age          = age;
        this.gender       = gender;
        this.email        = email;
        this.mobileNumber = mobileNumber;
        this.address      = address;
        this.isDeleted    = isDeleted;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
        this.createdBy    = createdBy;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int     getId()                      { return id; }
    public void    setId(int id)                { this.id = id; }

    public String  getName()                    { return name; }
    public void    setName(String n)            { this.name = n; }

    public int     getAge()                     { return age; }
    public void    setAge(int a)                { this.age = a; }

    public String  getGender()                  { return gender; }
    public void    setGender(String g)          { this.gender = g; }

    public String  getEmail()                   { return email; }
    public void    setEmail(String e)           { this.email = e; }

    public String  getMobileNumber()            { return mobileNumber; }
    public void    setMobileNumber(String m)    { this.mobileNumber = m; }

    public String  getAddress()                 { return address; }
    public void    setAddress(String a)         { this.address = a; }

    public boolean isDeleted()                  { return isDeleted; }
    public void    setDeleted(boolean d)        { this.isDeleted = d; }

    public String  getCreatedAt()               { return createdAt; }
    public void    setCreatedAt(String s)       { this.createdAt = s; }

    public String  getUpdatedAt()               { return updatedAt; }
    public void    setUpdatedAt(String s)       { this.updatedAt = s; }

    public String  getCreatedBy()               { return createdBy; }
    public void    setCreatedBy(String s)       { this.createdBy = s; }

    // ── toString ──────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "Patient{id=%d, name='%s', age=%d, gender='%s', mobile='%s', email='%s'}",
            id, name, age, gender, mobileNumber, email);
    }
}
