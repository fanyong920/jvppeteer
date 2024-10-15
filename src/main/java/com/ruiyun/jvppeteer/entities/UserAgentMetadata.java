package com.ruiyun.jvppeteer.entities;

import java.util.List;
/**
 * 用于指定要模拟的 User Agent Client Hints。详见 <a href="https://wicg.github.io/ua-client-hints">详见这里</a>
 * 缺失的可选值将由目标系统使用其通常的值填充。
 */
public class UserAgentMetadata {
    /**
     * 品牌出现在 Sec-CH-UA 中。
     */
    private List<UserAgentBrandVersion> brands;
    /**
     * 品牌出现在 Sec-CH-UA-Full-Version-List 中。
     */
    private List<UserAgentBrandVersion> fullVersionList;
    private String fullVersion;
    private String platform;
    private String platformVersion;
    private String architecture;
    private String model;
    private boolean mobile;
    private String bitness;
    private boolean wow64;

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public boolean getWow64() {
        return wow64;
    }

    public void setWow64(boolean wow64) {
        this.wow64 = wow64;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<UserAgentBrandVersion> getFullVersionList() {
        return fullVersionList;
    }

    public void setFullVersionList(List<UserAgentBrandVersion> fullVersionList) {
        this.fullVersionList = fullVersionList;
    }

    public boolean getMobile() {
        return mobile;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    public List<UserAgentBrandVersion> getBrands() {
        return brands;
    }

    public void setBrands(List<UserAgentBrandVersion> brands) {
        this.brands = brands;
    }

    public String getBitness() {
        return bitness;
    }

    public void setBitness(String bitness) {
        this.bitness = bitness;
    }

    public String getFullVersion() {
        return fullVersion;
    }

    public void setFullVersion(String fullVersion) {
        this.fullVersion = fullVersion;
    }
}
