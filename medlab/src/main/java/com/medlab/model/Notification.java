package com.medlab.model;

/**
 * POJO representing a Notification sent to a patient.
 *
 * Types: ORDER_CREATED, SAMPLE_COLLECTED, REPORT_READY, GENERAL, ORDER_CANCELLED
 *
 * Includes is_read tracking, audit fields, soft-delete.
 */
public class Notification {

    public enum Type {
        ORDER_CREATED,
        SAMPLE_COLLECTED,
        REPORT_READY,
        GENERAL,
        ORDER_CANCELLED
    }

    private int     id;
    private int     patientId;
    private String  message;
    private Type    type;
    private boolean isRead;
    private boolean isDeleted;
    private String  createdAt;
    private String  updatedAt;
    private String  createdBy;

    // ── Constructors ──────────────────────────────────────────────

    public Notification() {}

    public Notification(int patientId, String message, Type type, String createdBy) {
        this.patientId = patientId;
        this.message   = message;
        this.type      = type;
        this.isRead    = false;
        this.isDeleted = false;
        this.createdBy = createdBy;
    }

    public Notification(int id, int patientId, String message, String type,
                        boolean isRead, boolean isDeleted,
                        String createdAt, String updatedAt, String createdBy) {
        this.id        = id;
        this.patientId = patientId;
        this.message   = message;
        this.type      = Type.valueOf(type);
        this.isRead    = isRead;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int     getId()                  { return id; }
    public void    setId(int id)            { this.id = id; }

    public int     getPatientId()           { return patientId; }
    public void    setPatientId(int p)      { this.patientId = p; }

    public String  getMessage()             { return message; }
    public void    setMessage(String m)     { this.message = m; }

    public Type    getType()                { return type; }
    public void    setType(Type t)          { this.type = t; }

    public boolean isRead()                 { return isRead; }
    public void    setRead(boolean r)       { this.isRead = r; }

    public boolean isDeleted()              { return isDeleted; }
    public void    setDeleted(boolean d)    { this.isDeleted = d; }

    public String  getCreatedAt()           { return createdAt; }
    public void    setCreatedAt(String s)   { this.createdAt = s; }

    public String  getUpdatedAt()           { return updatedAt; }
    public void    setUpdatedAt(String s)   { this.updatedAt = s; }

    public String  getCreatedBy()           { return createdBy; }
    public void    setCreatedBy(String s)   { this.createdBy = s; }

    // ── toString ──────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "Notification{id=%d, patientId=%d, type=%s, read=%b, message='%s', sentAt='%s'}",
            id, patientId, type, isRead, message, createdAt);
    }
}
