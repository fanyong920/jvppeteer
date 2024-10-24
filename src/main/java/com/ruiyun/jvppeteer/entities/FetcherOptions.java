package com.ruiyun.jvppeteer.entities;

import com.ruiyun.jvppeteer.common.ChromeReleaseChannel;
import com.ruiyun.jvppeteer.common.Product;

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
     * 浏览器的版本，格式是xxx.xxx.xxx.xxx，一共四位数，例如 80.0.3987.87
     * <p>
     * 每个版本已经特属于某个 milestone，也特属于某个 channel，也特属于某个 build
     * <p>
     * 因此，指定了版本，就不用配置 milestone channel build 了
     */
    private String version;

    /**
     * 浏览器的渠道，例如：stable, dev, beta, canary，latest(特属于 CHROMIUM)。
     * <p>
     * 如果配置此项并没有指定 version，那么会下载该渠道下的最新版本的浏览器。
     * <p>
     * channel milestone  build 三个配置选择其一就可以了。
     */
    private ChromeReleaseChannel channel;

    /**
     * 里程碑号，如果配置此项并没有指定 version，那么会下载该里程碑号下的最新版本的浏览器。
     * <p>
     * chrome浏览器版本号是4位数，例如 80.0.3987.87，里程碑号是第1位数，例如 80。
     * <p>
     * 第一个数字：主版本号（里程碑号）：表示主要版本，每次增加都意味着有显著的新功能或重大改进。
     * <p>
     * 第二个数字：次版本号：表示次要更新，可能包括小的功能改进或安全修复。
     * <p>
     * 第三个数字：构建版本号：表示具体的构建版本，每次构建都会递增。
     * <p>
     * 第四个数字：修订版本号：表示特定构建的修订次数，用于修复已知问题或进行微调。
     * <p>
     * channel milestone  build 三个配置选择其一就可以了。
     */
    private String milestone;

    /**
     * 构建版本号，例如：112.0.23,即 version 前3位数
     * <p>
     * 如果配置此项并没有指定 version，那么会下载该构建版本号下的最新版本的浏览器。
     * <p>
     * channel milestone  build 三个配置选择其一就可以了。
     */
    private String build;

    public FetcherOptions() {
        super();
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

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    public ChromeReleaseChannel getChannel() {
        return channel;
    }

    public void setChannel(ChromeReleaseChannel channel) {
        this.channel = channel;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }
}
