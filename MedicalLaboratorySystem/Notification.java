package com.medlab.model;

/**
 * POJO representing a Notification sent to a patient.
 *
 * Type examples: ORDER_CREATED, SAMPLE_COLLECTED, REPORT_READY
 *
 * Future: becomes a JPA @Entity; type becomes an @Enumerated field.
 */
public class Notification {

    public enum Type {
        ORDER_CREATED,
        SAMPLE_COLLECTED,
        REPORT_READY,
        GENERAL
    }

    private int    id;
    private int    patientId;
    private String message;
    private Type   type;
    private String sentAt;

    // ── Constructors ──────────────────────────────────────────────

    public Notification() {}

    public Notification(int patientId, String message, Type type, String sentAt) {
        this.patientId = patientId;
        this.message   = message;
        this.type      = type;
        this.sentAt    = sentAt;
    }

    public Notification(int id, int patientId, String message, String type, String sentAt) {
        this.id        = id;
        this.patientId = patientId;
        this.message   = message;
        this.type      = Type.valueOf(type);
        this.sentAt    = sentAt;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int getId()                   { return id; }
    public void setId(int id)            { this.id = id; }

    public int getPatientId()            { return patientId; }
    public void setPatientId(int pid)    { this.patientId = pid; }

    public String getMessage()               { return message; }
    public void setMessage(String m)         { this.message = m; }

    public Type getType()                    { return type; }
    public void setType(Type t)              { this.type = t; }

    public String getSentAt()                { return sentAt; }
    public void setSentAt(String s)          { this.sentAt = s; }

    // ── toString ──────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "Notification{id=%d, patientId=%d, type=%s, message='%s', sentAt='%s'}",
            id, patientId, type, message, sentAt);
    }
}
