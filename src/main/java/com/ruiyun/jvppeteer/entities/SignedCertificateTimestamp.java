package com.ruiyun.jvppeteer.entities;

public class SignedCertificateTimestamp {
    /**
     * Validation status.
     */
    private String status;

    /**
     * Origin.
     */
    private String origin;

    /**
     * Log name / description.
     */
    private String logDescription;

    /**
     * Log ID.
     */
    private String logId;

    /**
     * Issuance date. Unlike TimeSinceEpoch, this contains the number of
     * milliseconds since January 1, 1970, UTC, not the number of seconds.
     */
    private long timestamp;

    /**
     * Hash algorithm.
     */
    private String hashAlgorithm;

    /**
     * Signature algorithm.
     */
    private String signatureAlgorithm;

    /**
     * Signature data.
     */
    private String signatureData;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getSignatureData() {
        return signatureData;
    }

    public void setSignatureData(String signatureData) {
        this.signatureData = signatureData;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getLogDescription() {
        return logDescription;
    }

    public void setLogDescription(String logDescription) {
        this.logDescription = logDescription;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
