package com.ruiyun.jvppeteer.protocol.network;

/**
 * Deletes browser cookies with matching name and url or domain/path pair.
 */
public class DeleteCookiesParameters {

    /**
     * Name of the cookies to remove.
     */
    private String name;
    /**
     * If specified, deletes all the cookies with the given name where domain and path match
     provided URL.
     */
    private String url;
    /**
     * If specified, deletes only cookies with the exact domain.
     */
    private String domain;
    /**
     * If specified, deletes only cookies with the exact path.
     */
    private String path;

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
}
