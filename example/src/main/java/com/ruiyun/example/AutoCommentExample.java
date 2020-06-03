package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoCommentExample {

    public static void main(String[] args) throws Exception {
        Browser browser = lauch("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
        Page page = browser.newPage();
        PageNavigateOptions navigateOptions = new PageNavigateOptions();
        //如果不设置 domcontentloaded 算页面导航完成的话，那么goTo方法会超时，因为图片请求被拦截了，页面不会达到loaded阶段
        navigateOptions.setWaitUntil(Collections.singletonList("domcontentloaded"));
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
