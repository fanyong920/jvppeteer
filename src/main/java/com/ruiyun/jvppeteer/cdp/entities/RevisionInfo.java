package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.common.Product;

public class RevisionInfo {

    private String revision;

    private String executablePath;

    private String folderPath;

    private boolean local;

    private String url;

    private Product product;

    public RevisionInfo() {
    }

    public RevisionInfo(String revision, String executablePath, String folderPath, boolean local, String url, Product product) {
        this.revision = revision;
        this.executablePath = executablePath;
        this.folderPath = folderPath;
        this.local = local;
        this.url = url;
        this.product = product;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public boolean getLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "RevisionInfo{" +
                "revision='" + revision + '\'' +
                ", executablePath='" + executablePath + '\'' +
                ", folderPath='" + folderPath + '\'' +
                ", local=" + local +
                ", url='" + url + '\'' +
                ", product='" + product + '\'' +
                '}';
    }
}
