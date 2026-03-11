package com.medlab.model;

/**
 * POJO representing a Lab Test Order.
 *
 * Status lifecycle: PENDING → SAMPLE_COLLECTED → IN_PROGRESS → COMPLETED
 *
 * Future: becomes a JPA @Entity
 */
public class TestOrder {

    public enum Status {
        PENDING,
        SAMPLE_COLLECTED,
        IN_PROGRESS,
        COMPLETED
    }

    private int    id;
    private int    patientId;
    private String testName;
    private String orderedBy;       // Doctor name
    private Status status;
    private String orderedDate;

    // ── Constructors ──────────────────────────────────────────────

    public TestOrder() {}

    public TestOrder(int patientId, String testName, String orderedBy, String orderedDate) {
        this.patientId   = patientId;
        this.testName    = testName;
        this.orderedBy   = orderedBy;
        this.status      = Status.PENDING;
        this.orderedDate = orderedDate;
    }

    public TestOrder(int id, int patientId, String testName, String orderedBy,
                     String status, String orderedDate) {
        this.id          = id;
        this.patientId   = patientId;
        this.testName    = testName;
        this.orderedBy   = orderedBy;
        this.status      = Status.valueOf(status);
        this.orderedDate = orderedDate;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int getId()                   { return id; }
    public void setId(int id)            { this.id = id; }

    public int getPatientId()            { return patientId; }
    public void setPatientId(int pid)    { this.patientId = pid; }

    public String getTestName()              { return testName; }
    public void setTestName(String t)        { this.testName = t; }

    public String getOrderedBy()             { return orderedBy; }
    public void setOrderedBy(String o)       { this.orderedBy = o; }

    public Status getStatus()                { return status; }
    public void setStatus(Status s)          { this.status = s; }

    public String getOrderedDate()           { return orderedDate; }
    public void setOrderedDate(String d)     { this.orderedDate = d; }

    // ── toString ──────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "TestOrder{id=%d, patientId=%d, test='%s', orderedBy='%s', status=%s, date='%s'}",
            id, patientId, testName, orderedBy, status, orderedDate);
    }
}
