package com.medlab.model;

/**
 * POJO representing a Patient.
 *
 * Future: becomes a JPA @Entity with @Table(name="patients")
 */
public class Patient {

    private int id;
    private String name;
    private int age;
    private String gender;   // MALE | FEMALE | OTHER
    private String contact;

    // ── Constructors ──────────────────────────────────────────────

    public Patient() {}

    public Patient(String name, int age, String gender, String contact) {
        this.name    = name;
        this.age     = age;
        this.gender  = gender;
        this.contact = contact;
    }

    public Patient(int id, String name, int age, String gender, String contact) {
        this.id      = id;
        this.name    = name;
        this.age     = age;
        this.gender  = gender;
        this.contact = contact;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }

    public String getName()              { return name; }
    public void setName(String name)     { this.name = name; }

    public int getAge()              { return age; }
    public void setAge(int age)      { this.age = age; }

    public String getGender()            { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getContact()               { return contact; }
    public void setContact(String contact)   { this.contact = contact; }

    // ── toString ──────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("Patient{id=%d, name='%s', age=%d, gender='%s', contact='%s'}",
                id, name, age, gender, contact);
    }
}
