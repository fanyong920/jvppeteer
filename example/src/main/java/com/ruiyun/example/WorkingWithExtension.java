package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Target;

import java.util.ArrayList;
import java.util.List;

public class WorkingWithExtension {

    public static void main(String[] args) throws Exception {
        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);

        ArrayList<String> additionalArgs = new ArrayList<>();
        additionalArgs.add("--no-sandbox");
        additionalArgs.add("--disable-setuid-sandbox");
        //指定插件所在的文件夹,如果手上暂时没有插件，可以我个人的插件https://github.com/fanyong920/crawlItem.git 克隆下来即可
        String pathToExtension = "E:\\workspace\\crawlItem";
        additionalArgs.add("--disable-extensions-except="+pathToExtension);
        additionalArgs.add("--load-extension="+pathToExtension);

        //插件的加载在有头模式下才能生效
        Browser browser = Puppeteer.launch(false);

        List<Target> targets =  browser.targets();
        for (Target target : targets) {
            if("background_page".equals(target.type())){
                System.out.println("目标名称=" + target.getTargetInfo().getTitle());
                Page backgroundPage = target.page();
            }
        }
        browser.close();
    }
}
