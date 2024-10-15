package com.ruiyun.jvppeteer.entities;

import com.ruiyun.jvppeteer.common.Product;

import java.net.Proxy;

public class FetcherOptions {
    /**
     * 下载哪个平台的浏览器，可以空缺
     */
    private String platform;
    /**
     * 下载的浏览器存放的文件夹，可以空缺
     * <p>
     * 如果空缺，那么项目根目录下的.local-browser作为存放文件夹
     */
    private String cacheDir;
    /**
     * 下载的服务器地址，例如 https://storage.googleapis.com，可以空缺
     */
    private String host;
    /**
     * 下载哪种类型的chrome浏览器，默认是chrome
     */
    private Product product = Product.CHROME;
    /**
     * 浏览器的版本
     */
    private String version;
    /**
     * 下载代理
     */
    private Proxy proxy;

    public FetcherOptions() {
        super();
    }

    public FetcherOptions(String platform, String cacheDir, String host, Product product, String version) {
        super();
        this.platform = platform;
        this.cacheDir = cacheDir;
        this.host = host;
        this.product = product;
        this.version = version;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }
}
