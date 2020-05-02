package com.ruiyun.test;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.options.FetcherOptions;
import com.ruiyun.jvppeteer.types.browser.BrowserFetcher;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.function.BiConsumer;

public class DownloadChromiumExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        Puppeteer puppeteer = new Puppeteer();
        FetcherOptions options = new FetcherOptions();

        BrowserFetcher browserFetcher = puppeteer.createBrowserFetcher(options);
        BiConsumer<Integer,Integer> processCallback = (a,b) -> {
            NumberFormat percent = NumberFormat.getPercentInstance();
            percent.setMaximumFractionDigits(2);
            System.out.println("下载进度："+ percent.format(new BigDecimal(a).divide(new BigDecimal(b)).doubleValue()));
        };
        browserFetcher.download("737027",processCallback);
    }
}
