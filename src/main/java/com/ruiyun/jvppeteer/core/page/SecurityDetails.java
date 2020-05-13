package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.protocol.network.SecurityDetailsPayload;

public class SecurityDetails {

    private String subjectName;

    private String issuer;

    private Double validFrom;

    private Double validTo;

    private String protocol;

    public SecurityDetails() {
    }

    public SecurityDetails(SecurityDetailsPayload securityDetails) {
        this.subjectName = securityDetails.getSubjectName();
        this.issuer = securityDetails.getIssuer();
        this.validFrom = securityDetails.getValidFrom();
        this.validTo = securityDetails.getValidTo();
        this.protocol = securityDetails.getProtocol();
    }

    public String subjectName() {
        return this.subjectName;
    }

    public String issuer() {
        return this.issuer;
    }

    public double validFrom() {
        return this.validFrom;
    }

    public double validTo() {
        return this.validTo;
    }

    public String protocol() {
        return this.protocol;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Double getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Double validFrom) {
        this.validFrom = validFrom;
    }

    public Double getValidTo() {
        return validTo;
    }

    public void setValidTo(Double validTo) {
        this.validTo = validTo;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
