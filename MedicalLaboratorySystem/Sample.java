package com.medlab.model;

/**
 * POJO representing a biological Sample collected for a test order.
 *
 * Status lifecycle: COLLECTED → PROCESSING → ANALYSED
 *
 * Future: becomes a JPA @Entity
 */
public class Sample {

    public enum Status {
        COLLECTED,
        PROCESSING,
        ANALYSED
    }

    private int    id;
    private int    orderId;
    private String sampleType;      // e.g. BLOOD, URINE, STOOL, SWAB
    private String collectedBy;     // Lab technician name
    private String collectedDate;
    private Status status;

    // ── Constructors ──────────────────────────────────────────────

    public Sample() {}

    public Sample(int orderId, String sampleType, String collectedBy, String collectedDate) {
        this.orderId       = orderId;
        this.sampleType    = sampleType;
        this.collectedBy   = collectedBy;
        this.collectedDate = collectedDate;
        this.status        = Status.COLLECTED;
    }

    public Sample(int id, int orderId, String sampleType, String collectedBy,
                  String collectedDate, String status) {
        this.id            = id;
        this.orderId       = orderId;
        this.sampleType    = sampleType;
        this.collectedBy   = collectedBy;
        this.collectedDate = collectedDate;
        this.status        = Status.valueOf(status);
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int getId()                   { return id; }
    public void setId(int id)            { this.id = id; }

    public int getOrderId()              { return orderId; }
    public void setOrderId(int oid)      { this.orderId = oid; }

    public String getSampleType()            { return sampleType; }
    public void setSampleType(String t)      { this.sampleType = t; }

    public String getCollectedBy()           { return collectedBy; }
    public void setCollectedBy(String c)     { this.collectedBy = c; }

    public String getCollectedDate()         { return collectedDate; }
    public void setCollectedDate(String d)   { this.collectedDate = d; }

    public Status getStatus()                { return status; }
    public void setStatus(Status s)          { this.status = s; }

    // ── toString ──────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "Sample{id=%d, orderId=%d, type='%s', collectedBy='%s', date='%s', status=%s}",
            id, orderId, sampleType, collectedBy, collectedDate, status);
    }
}
