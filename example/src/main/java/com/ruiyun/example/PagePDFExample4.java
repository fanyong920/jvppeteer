package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.PDFOptions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PagePDFExample4 {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        //自动下载，第一次下载后不会再下载
//        BrowserFetcher.downloadIfNotExist(null);

        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();
        //生成pdf必须在无厘头模式下才能生效
//        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        PDFOptions pdfOptions = new PDFOptions();

        //获取到pdf的字节数组
        byte[] bytes = page.pdf(pdfOptions);
        byte[] buffer = new byte[Constant.DEFAULT_BUFFER_SIZE];

        FileOutputStream fileOutputStream = new FileOutputStream(new File("bytes.pdf"));
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        BufferedInputStream reader = new BufferedInputStream(byteArrayInputStream);
        BufferedOutputStream writer = new BufferedOutputStream(fileOutputStream);
        int read;
        while ((read = reader.read(buffer, 0, Constant.DEFAULT_BUFFER_SIZE)) != -1) {
            writer.write(buffer, 0, read);
            writer.flush();
        }
        page.close();
        browser.close();
    }
}
