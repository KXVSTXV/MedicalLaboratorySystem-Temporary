package com.medlab.model;

/**
 * POJO representing a Lab Report.
 *
 * Status lifecycle: DRAFT → VERIFIED → RELEASED
 *
 * Includes: normal_range comparison, is_abnormal flag, verified_by tracking.
 * Includes audit fields and soft-delete.
 */
public class Report {

    public enum Status {
        DRAFT,
        VERIFIED,
        RELEASED
    }

    private int     id;
    private int     orderId;
    private String  result;
    private String  remarks;
    private String  normalRange;
    private boolean isAbnormal;
    private String  preparedBy;
    private String  verifiedBy;
    private String  reportDate;
    private Status  status;
    private boolean isDeleted;
    private String  createdAt;
    private String  updatedAt;
    private String  createdBy;

    // ── Constructors ──────────────────────────────────────────────

    public Report() {}

    public Report(int orderId, String result, String remarks,
                  String normalRange, boolean isAbnormal,
                  String preparedBy, String reportDate, String createdBy) {
        this.orderId     = orderId;
        this.result      = result;
        this.remarks     = remarks;
        this.normalRange = normalRange;
        this.isAbnormal  = isAbnormal;
        this.preparedBy  = preparedBy;
        this.reportDate  = reportDate;
        this.status      = Status.DRAFT;
        this.isDeleted   = false;
        this.createdBy   = createdBy;
    }

    public Report(int id, int orderId, String result, String remarks,
                  String normalRange, boolean isAbnormal, String preparedBy,
                  String verifiedBy, String reportDate, String status,
                  boolean isDeleted, String createdAt, String updatedAt, String createdBy) {
        this.id          = id;
        this.orderId     = orderId;
        this.result      = result;
        this.remarks     = remarks;
        this.normalRange = normalRange;
        this.isAbnormal  = isAbnormal;
        this.preparedBy  = preparedBy;
        this.verifiedBy  = verifiedBy;
        this.reportDate  = reportDate;
        this.status      = Status.valueOf(status);
        this.isDeleted   = isDeleted;
        this.createdAt   = createdAt;
        this.updatedAt   = updatedAt;
        this.createdBy   = createdBy;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int     getId()                      { return id; }
    public void    setId(int id)                { this.id = id; }

    public int     getOrderId()                 { return orderId; }
    public void    setOrderId(int oid)          { this.orderId = oid; }

    public String  getResult()                  { return result; }
    public void    setResult(String r)          { this.result = r; }

    public String  getRemarks()                 { return remarks; }
    public void    setRemarks(String r)         { this.remarks = r; }

    public String  getNormalRange()             { return normalRange; }
    public void    setNormalRange(String n)     { this.normalRange = n; }

    public boolean isAbnormal()                 { return isAbnormal; }
    public void    setAbnormal(boolean a)       { this.isAbnormal = a; }

    public String  getPreparedBy()              { return preparedBy; }
    public void    setPreparedBy(String p)      { this.preparedBy = p; }

    public String  getVerifiedBy()              { return verifiedBy; }
    public void    setVerifiedBy(String v)      { this.verifiedBy = v; }

    public String  getReportDate()              { return reportDate; }
    public void    setReportDate(String d)      { this.reportDate = d; }

    public Status  getStatus()                  { return status; }
    public void    setStatus(Status s)          { this.status = s; }

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
            "Report{id=%d, orderId=%d, result='%s', normalRange='%s', abnormal=%b, preparedBy='%s', status=%s}",
            id, orderId, result, normalRange, isAbnormal, preparedBy, status);
    }
}
