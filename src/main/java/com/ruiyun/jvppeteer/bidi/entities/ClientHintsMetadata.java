package com.ruiyun.jvppeteer.bidi.entities;

import com.ruiyun.jvppeteer.cdp.entities.UserAgentBrandVersion;
import com.ruiyun.jvppeteer.cdp.entities.UserAgentMetadata;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.List;

public class ClientHintsMetadata {
    private List<BrandVersion> brands;
    private List<BrandVersion> fullVersionList;
    private String platform;
    private String platformVersion;
    private String architecture;
    private String model;
    private boolean mobile;
    private String bitness;
    private boolean wow64;
    private List<String> formFactors;

    public List<BrandVersion> getBrands() {
        return brands;
    }

    public void setBrands(List<BrandVersion> brands) {
        this.brands = brands;
    }

    public List<BrandVersion> getFullVersionList() {
        return fullVersionList;
    }

    public void setFullVersionList(List<BrandVersion> fullVersionList) {
        this.fullVersionList = fullVersionList;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean getMobile() {
        return mobile;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    public String getBitness() {
        return bitness;
    }

    public void setBitness(String bitness) {
        this.bitness = bitness;
    }

    public boolean getWow64() {
        return wow64;
    }

    public void setWow64(boolean wow64) {
        this.wow64 = wow64;
    }

    public List<String> getFormFactors() {
        return formFactors;
    }

    public void setFormFactors(List<String> formFactors) {
        this.formFactors = formFactors;
    }

    /**
     * 从 UserAgentMetadata 创建 ClientHintsMetadata 对象
     * 将相同属性的值拷贝到新对象中
     *
     * @param userAgentMetadata 源 UserAgentMetadata 对象
     * @return 新的 ClientHintsMetadata 对象
     */
    public static ClientHintsMetadata fromUserAgentMetadata(UserAgentMetadata userAgentMetadata) {
        if (userAgentMetadata == null) {
            return null;
        }

        ClientHintsMetadata clientHints = new ClientHintsMetadata();
        
        // 转换 brands 列表
        if (ValidateUtil.isNotEmpty(userAgentMetadata.getBrands())) {
            clientHints.setBrands(convertBrandVersions(userAgentMetadata.getBrands()));
        }
        
        // 转换 fullVersionList 列表
        if (ValidateUtil.isNotEmpty(userAgentMetadata.getFullVersionList())) {
            clientHints.setFullVersionList(convertBrandVersions(userAgentMetadata.getFullVersionList()));
        }
        
        // 拷贝相同类型的属性
        clientHints.setPlatform(userAgentMetadata.getPlatform());
        clientHints.setPlatformVersion(userAgentMetadata.getPlatformVersion());
        clientHints.setArchitecture(userAgentMetadata.getArchitecture());
        clientHints.setModel(userAgentMetadata.getModel());
        clientHints.setMobile(userAgentMetadata.getMobile());
        clientHints.setBitness(userAgentMetadata.getBitness());
        clientHints.setWow64(userAgentMetadata.getWow64());
        
        // 注意：fullVersion 和 formFactors 字段不兼容，不进行拷贝
        
        return clientHints;
    }

    /**
     * 将 UserAgentBrandVersion 列表转换为 BrandVersion 列表
     *
     * @param source 源 UserAgentBrandVersion 列表
     * @return 转换后的 BrandVersion 列表
     */
    private static List<BrandVersion> convertBrandVersions(List<UserAgentBrandVersion> source) {
        if (source == null) {
            return null;
        }
        
        List<BrandVersion> target = new ArrayList<>();
        for (UserAgentBrandVersion item : source) {
            BrandVersion brandVersion = new BrandVersion();
            brandVersion.setBrand(item.getBrand());
            brandVersion.setVersion(item.getVersion());
            target.add(brandVersion);
        }
        return target;
    }
}
