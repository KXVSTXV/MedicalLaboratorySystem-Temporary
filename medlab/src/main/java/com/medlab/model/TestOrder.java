package com.medlab.model;

/**
 * POJO representing a Lab Test Order.
 *
 * Status lifecycle: ORDERED → SAMPLE_COLLECTED → IN_PROGRESS → REPORT_READY → COMPLETED
 *                                                                             → CANCELLED
 *
 * Includes audit fields and soft-delete.
 *
 * Future: becomes a JPA @Entity
 */
public class TestOrder {

    public enum Status {
        ORDERED,
        SAMPLE_COLLECTED,
        IN_PROGRESS,
        REPORT_READY,
        COMPLETED,
        CANCELLED
    }

    public enum Priority {
        ROUTINE,
        URGENT,
        STAT
    }

    private int      id;
    private int      patientId;
    private String   testName;
    private String   orderedBy;
    private Status   status;
    private Priority priority;
    private String   notes;
    private boolean  isDeleted;
    private String   createdAt;
    private String   updatedAt;
    private String   createdBy;

    // ── Constructors ──────────────────────────────────────────────

    public TestOrder() {}

    public TestOrder(int patientId, String testName, String orderedBy,
                     Priority priority, String notes, String createdBy) {
        this.patientId = patientId;
        this.testName  = testName;
        this.orderedBy = orderedBy;
        this.status    = Status.ORDERED;
        this.priority  = priority;
        this.notes     = notes;
        this.isDeleted = false;
        this.createdBy = createdBy;
    }

    public TestOrder(int id, int patientId, String testName, String orderedBy,
                     String status, String priority, String notes,
                     boolean isDeleted, String createdAt, String updatedAt, String createdBy) {
        this.id        = id;
        this.patientId = patientId;
        this.testName  = testName;
        this.orderedBy = orderedBy;
        this.status    = Status.valueOf(status);
        this.priority  = Priority.valueOf(priority);
        this.notes     = notes;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int      getId()                      { return id; }
    public void     setId(int id)                { this.id = id; }

    public int      getPatientId()               { return patientId; }
    public void     setPatientId(int p)          { this.patientId = p; }

    public String   getTestName()                { return testName; }
    public void     setTestName(String t)        { this.testName = t; }

    public String   getOrderedBy()               { return orderedBy; }
    public void     setOrderedBy(String o)       { this.orderedBy = o; }

    public Status   getStatus()                  { return status; }
    public void     setStatus(Status s)          { this.status = s; }

    public Priority getPriority()                { return priority; }
    public void     setPriority(Priority p)      { this.priority = p; }

    public String   getNotes()                   { return notes; }
    public void     setNotes(String n)           { this.notes = n; }

    public boolean  isDeleted()                  { return isDeleted; }
    public void     setDeleted(boolean d)        { this.isDeleted = d; }

    public String   getCreatedAt()               { return createdAt; }
    public void     setCreatedAt(String s)       { this.createdAt = s; }

    public String   getUpdatedAt()               { return updatedAt; }
    public void     setUpdatedAt(String s)       { this.updatedAt = s; }

    public String   getCreatedBy()               { return createdBy; }
    public void     setCreatedBy(String s)       { this.createdBy = s; }

    // ── toString ──────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "TestOrder{id=%d, patientId=%d, test='%s', orderedBy='%s', status=%s, priority=%s, createdAt='%s'}",
            id, patientId, testName, orderedBy, status, priority, createdAt);
    }
}
