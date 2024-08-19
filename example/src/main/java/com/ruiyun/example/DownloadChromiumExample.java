package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

public class DownloadChromiumExample {
    /**
     * 演示下载chrome功能，该方法同样适用于linux,如果在生产环境上不想自己安装chromium，可以使用此方法下载
     * @param args
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, URISyntaxException {
        Puppeteer puppeteer = new Puppeteer();
        BrowserFetcher browserFetcher = puppeteer.createBrowserFetcher();
        //指定下载版本下载，也可以不指定下载版本，如果不知道下载版本，则默认下载最新版本
        browserFetcher.download();
        System.out.println("下载完成");
    }
}
