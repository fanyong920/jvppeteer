package com.ruiyun.jvppeteer.protocol.network;

/**
 * Details of a signed certificate timestamp (SCT).
 */
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
     * Issuance date.
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

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getLogDescription() {
        return logDescription;
    }

    public void setLogDescription(String logDescription) {
        this.logDescription = logDescription;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
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
}
