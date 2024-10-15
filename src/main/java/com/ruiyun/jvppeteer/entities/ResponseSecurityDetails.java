package com.ruiyun.jvppeteer.entities;

import java.util.List;

public class ResponseSecurityDetails {
    private String subjectName;
    private String issuer;
    private long validFrom;
    private long validTo;
    private String protocol;
    private List<String> sanList;

    public ResponseSecurityDetails() {}

    public ResponseSecurityDetails(com.ruiyun.jvppeteer.entities.SecurityDetails securityPayload) {
        this.subjectName = securityPayload.getSubjectName();
        this.issuer = securityPayload.getIssuer();
        this.validFrom = securityPayload.getValidFrom();
        this.validTo = securityPayload.getValidTo();
        this.protocol = securityPayload.getProtocol();
        this.sanList = securityPayload.getSanList();
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

    public long getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(long validFrom) {
        this.validFrom = validFrom;
    }

    public long getValidTo() {
        return validTo;
    }

    public void setValidTo(long validTo) {
        this.validTo = validTo;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<String> getSanList() {
        return sanList;
    }

    public void setSanList(List<String> sanList) {
        this.sanList = sanList;
    }
}
