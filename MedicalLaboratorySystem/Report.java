package com.medlab.model;

/**
 * POJO representing a Lab Report generated from a test order.
 *
 * Status lifecycle: DRAFT → VERIFIED → RELEASED
 *
 * Future: becomes a JPA @Entity
 */
public class Report {

    public enum Status {
        DRAFT,
        VERIFIED,
        RELEASED
    }

    private int    id;
    private int    orderId;
    private String result;
    private String remarks;
    private String preparedBy;
    private String reportDate;
    private Status status;

    // ── Constructors ──────────────────────────────────────────────

    public Report() {}

    public Report(int orderId, String result, String remarks,
                  String preparedBy, String reportDate) {
        this.orderId    = orderId;
        this.result     = result;
        this.remarks    = remarks;
        this.preparedBy = preparedBy;
        this.reportDate = reportDate;
        this.status     = Status.DRAFT;
    }

    public Report(int id, int orderId, String result, String remarks,
                  String preparedBy, String reportDate, String status) {
        this.id         = id;
        this.orderId    = orderId;
        this.result     = result;
        this.remarks    = remarks;
        this.preparedBy = preparedBy;
        this.reportDate = reportDate;
        this.status     = Status.valueOf(status);
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int getId()                   { return id; }
    public void setId(int id)            { this.id = id; }

    public int getOrderId()              { return orderId; }
    public void setOrderId(int oid)      { this.orderId = oid; }

    public String getResult()                { return result; }
    public void setResult(String r)          { this.result = r; }

    public String getRemarks()               { return remarks; }
    public void setRemarks(String r)         { this.remarks = r; }

    public String getPreparedBy()            { return preparedBy; }
    public void setPreparedBy(String p)      { this.preparedBy = p; }

    public String getReportDate()            { return reportDate; }
    public void setReportDate(String d)      { this.reportDate = d; }

    public Status getStatus()                { return status; }
    public void setStatus(Status s)          { this.status = s; }

    // ── toString ──────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "Report{id=%d, orderId=%d, result='%s', remarks='%s', preparedBy='%s', date='%s', status=%s}",
            id, orderId, result, remarks, preparedBy, reportDate, status);
    }
}
