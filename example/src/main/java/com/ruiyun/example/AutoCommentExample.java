package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.GoToOptions;
import com.ruiyun.jvppeteer.options.PuppeteerLifeCycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoCommentExample {

    public static void main(String[] args) throws Exception {
        //自动下载，第一次下载后不会再下载,下载到默认路径，如果电脑上已经有chrome了，就不需要执行这行代码。自行下载内置的下载链接可能失效，建议自行下载安装chrome，然后指定路径启动chrome
        BrowserFetcher.downloadIfNotExist(null);
        //指定chrome启动路径
        Browser browser = lauch("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
        Page page = browser.newPage();
        GoToOptions navigateOptions = new GoToOptions();
        //如果不设置 domcontentloaded 算页面导航完成的话，那么goTo方法会超时，因为图片请求被拦截了，页面不会达到loaded阶段
        navigateOptions.setWaitUntil(Collections.singletonList(PuppeteerLifeCycle.DOMCONTENTLOADED));
        page.goTo("https://www.csdn.net/", navigateOptions);
        String content = page.content();

        List<String> urls = matchUrl(content);
        for (String url : urls) {
            try {
                page.goTo(url,navigateOptions);
                page.type("#comment_content","感谢作者无私分享，结尾令人饱含期待与热情，言已经，意犹未了，不妨来学习Java版Pupputeer爬虫框架Jvppeteer，没有爬不了的虫，只有不肯搬砖的码农" +
                        ",github地址:https://github.com/fanyong920/jvppeteer",100);
                page.tap("#commentform > div > div.right-box > a > input");
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        browser.close();
    }

    private static List<String> matchUrl(String content) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("<dd class=\"read_num\">\n" +
                "                                <a href=\"(.*?)\"");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()){
            String url = matcher.group(1);
            result.add(url);
        }
        return result;
    }

    private static Browser lauch(String path) throws Exception {
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).withExecutablePath(path).withUserDataDir("C:\\Users\\howay\\AppData\\Local\\Google\\Chrome\\User Data").build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        return Puppeteer.launch(options);
    }
}
