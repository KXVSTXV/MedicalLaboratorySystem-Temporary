package com.medlab.model;

/**
 * POJO representing a biological Sample collected for a test order.
 *
 * Status lifecycle: COLLECTED → PROCESSING → ANALYSED | REJECTED
 *
 * Includes audit fields and soft-delete.
 */
public class Sample {

    public enum Status {
        COLLECTED,
        PROCESSING,
        ANALYSED,
        REJECTED
    }

    public enum SampleType {
        BLOOD, URINE, STOOL, SWAB, SPUTUM, CSF, OTHER
    }

    private int        id;
    private int        orderId;
    private SampleType sampleType;
    private String     collectedBy;
    private String     collectedDate;
    private Status     status;
    private String     rejectionReason;
    private boolean    isDeleted;
    private String     createdAt;
    private String     updatedAt;
    private String     createdBy;

    // ── Constructors ──────────────────────────────────────────────

    public Sample() {}

    public Sample(int orderId, String sampleType, String collectedBy,
                  String collectedDate, String createdBy) {
        this.orderId       = orderId;
        this.sampleType    = SampleType.valueOf(sampleType.toUpperCase());
        this.collectedBy   = collectedBy;
        this.collectedDate = collectedDate;
        this.status        = Status.COLLECTED;
        this.isDeleted     = false;
        this.createdBy     = createdBy;
    }

    public Sample(int id, int orderId, String sampleType, String collectedBy,
                  String collectedDate, String status, String rejectionReason,
                  boolean isDeleted, String createdAt, String updatedAt, String createdBy) {
        this.id              = id;
        this.orderId         = orderId;
        this.sampleType      = SampleType.valueOf(sampleType);
        this.collectedBy     = collectedBy;
        this.collectedDate   = collectedDate;
        this.status          = Status.valueOf(status);
        this.rejectionReason = rejectionReason;
        this.isDeleted       = isDeleted;
        this.createdAt       = createdAt;
        this.updatedAt       = updatedAt;
        this.createdBy       = createdBy;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int        getId()                         { return id; }
    public void       setId(int id)                   { this.id = id; }

    public int        getOrderId()                    { return orderId; }
    public void       setOrderId(int oid)             { this.orderId = oid; }

    public SampleType getSampleType()                 { return sampleType; }
    public void       setSampleType(SampleType t)     { this.sampleType = t; }

    public String     getCollectedBy()                { return collectedBy; }
    public void       setCollectedBy(String c)        { this.collectedBy = c; }

    public String     getCollectedDate()              { return collectedDate; }
    public void       setCollectedDate(String d)      { this.collectedDate = d; }

    public Status     getStatus()                     { return status; }
    public void       setStatus(Status s)             { this.status = s; }

    public String     getRejectionReason()            { return rejectionReason; }
    public void       setRejectionReason(String r)    { this.rejectionReason = r; }

    public boolean    isDeleted()                     { return isDeleted; }
    public void       setDeleted(boolean d)           { this.isDeleted = d; }

    public String     getCreatedAt()                  { return createdAt; }
    public void       setCreatedAt(String s)          { this.createdAt = s; }

    public String     getUpdatedAt()                  { return updatedAt; }
    public void       setUpdatedAt(String s)          { this.updatedAt = s; }

    public String     getCreatedBy()                  { return createdBy; }
    public void       setCreatedBy(String s)          { this.createdBy = s; }

    // ── toString ──────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "Sample{id=%d, orderId=%d, type=%s, collectedBy='%s', date='%s', status=%s}",
            id, orderId, sampleType, collectedBy, collectedDate, status);
    }
}
