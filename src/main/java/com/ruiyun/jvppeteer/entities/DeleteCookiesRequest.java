package com.ruiyun.jvppeteer.entities;

/**
 * Deletes browser cookies with matching name and url or domain/path pair.
 */
public class DeleteCookiesRequest {

    /**
     * 要删除的cookie名称
     */
    private String name;
    /**
     * 如果指定了该值，则删除与提供的URL匹配的所有具有给定名称的cookies。
     */
    private String url;

    /**
     * 如果指定了该值，则仅删除具有完全匹配域的cookies。
     */
    private String domain;

    /**
     * 如果指定了该值，则仅删除具有完全匹配路径的cookies。
     */
    private String path;

    /**
     * 如果指定了该值，则在给定的分区键中删除cookies。在Chrome中，分区键匹配分区cookie可用的顶级站点。
     * 在Firefox中，它匹配源起源（<a href="https://w3c.github.io/webdriver-bidi/#type-storage-PartitionKey">说明</a>）。
     */
    private CookiePartitionKey partitionKey;

    public DeleteCookiesRequest() {
        super();
    }

    public DeleteCookiesRequest(String name) {
        super();
        this.name = name;
    }

    public DeleteCookiesRequest(String name, String url, String domain, String path) {
        super();
        this.name = name;
        this.url = url;
        this.domain = domain;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public CookiePartitionKey getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(CookiePartitionKey partitionKey) {
        this.partitionKey = partitionKey;
    }

    @Override
    public String toString() {
        return "DeleteCookiesRequest{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", domain='" + domain + '\'' +
                ", path='" + path + '\'' +
                ", partitionKey=" + partitionKey +
                '}';
    }
}
