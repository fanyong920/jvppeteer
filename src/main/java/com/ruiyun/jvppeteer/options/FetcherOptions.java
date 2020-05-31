package com.ruiyun.jvppeteer.options;

public class FetcherOptions {

    private  String platform;

    private  String  path;

    private  String  host;

    private  String product;

    public FetcherOptions() {
        super();
    }

    public FetcherOptions(String platform, String path, String host, String product) {
        super();
        this.platform = platform;
        this.path = path;
        this.host = host;
        this.product = product;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

}
