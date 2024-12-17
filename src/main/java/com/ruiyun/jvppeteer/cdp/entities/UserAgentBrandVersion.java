package com.ruiyun.jvppeteer.cdp.entities;

public class UserAgentBrandVersion {
    private String brand;

    private String version;

    public UserAgentBrandVersion(String brand, String version) {
        this.brand = brand;
        this.version = version;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "UserAgentBrandVersion{" +
                "brand='" + brand + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
