package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.LaunchOptions;
import com.ruiyun.jvppeteer.cdp.entities.Protocol;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.Product;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.Test;

public class A_LaunchTest {
    public final LaunchOptions launchOptions = LaunchOptions.builder().
//            executablePath("C:\\Users\\fanyong\\Desktop\\typescriptPri\\.local-browser\\chrome-win32\\chrome-win32\\chrome.exe").
//            executablePath("C:\\Users\\fanyong\\Desktop\\typescriptPri\\.local-browser\\chrome-win32\\chrome-win32\\chrome.exe").product(Product.Chrome).
//        executablePath("C:\\Users\\fanyong\\Desktop\\jvppeteer\\example\\.local-browser\\win32-133.0\\core\\firefox.exe").
//        product(Product.Firefox).
            headless(false).
//            protocol(Protocol.CDP).
            //不设置窗口大小
                    defaultViewport(null).
            build();


    /**
     * 手动配置路径来启动浏览器
     * 优先级： 1 高
     */
    @Test
    public void test99() throws Exception {
        ArrayList<String> args = new ArrayList<>();//添加一些额外的启动参数
        args.add("--no-sandbox");
        launchOptions.setArgs(args);
        launchOptions.setDumpio(true);
//        launchOptions.setUserDataDir("C:\\Users\\fanyong\\Desktop\\typescriptPri");
        try (Browser browser = getBrowser()) {
            //打开一个页面
            Page page = browser.newPage();
            Target target1 = null;
            try {
                target1 = page.target();
            } catch (UnsupportedOperationException e) {
                System.out.println("webdriver bidi 不支持 page.target() 方法");
            }
            if (Objects.nonNull(target1)) {
                System.out.println("one type=" + target1.type() + ", url=" + target1.url());
            }
            List<Target> targets = browser.targets();
            //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
            for (Target target : targets) {
                System.out.println("two type=" + target.type() + ", url=" + target.url());
            }
            System.out.println("浏览器版本：" + browser.version());
        }
    }

    /**
     * 将浏览器启动路径写进环境变量中启动
     * 优先级： 2
     * {@link com.ruiyun.jvppeteer.common.Constant#EXECUTABLE_ENV}
     */
    @Test
    public void test98() throws Exception {
//        System.setProperty(Constant.EXECUTABLE_ENV[0], "C:\\Users\\fanyong\\Desktop\\jvppeteer\\example\\.local-browser\\win32-133.0\\core\\firefox.exe");
        //System.setProperty(Constant.EXECUTABLE_ENV[0], "C:\\Users\\fanyong\\Desktop\\jvppeteer\\example\\.local-browser\\win32-127.0.6533.99\\chrome-win32\\chrome.exe");
        //System.setProperty(Constant.EXECUTABLE_ENV[1], "C:\\Users\\fanyong\\Desktop\\jvppeteer\\example\\.local-browser\\win32-127.0.6533.99\\chrome-win32\\chrome.exe");
        //System.setProperty(Constant.EXECUTABLE_ENV[2], "C:\\Users\\fanyong\\Desktop\\jvppeteer\\example\\.local-browser\\win32-127.0.6533.99\\chrome-win32\\chrome.exe");
        try (Browser browser = getBrowser()) {
            //打开一个页面
            Page page = browser.newPage();
            Target target1 = null;
            try {
                target1 = page.target();
            } catch (UnsupportedOperationException e) {
                System.out.println("webdriver bidi 不支持 page.target() 方法");
            }
            if (Objects.nonNull(target1)) {
                System.out.println("one type=" + target1.type() + ", url=" + target1.url());
            }
            List<Target> targets = browser.targets();
            //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
            for (Target target : targets) {
                System.out.println("two type=" + target.type() + ", url=" + target.url());
            }
        }
    }

    /**
     * 配置启动的浏览器版本(首选版本）
     * 优先级： 3
     */
    @Test
    public void test10() throws Exception {
        launchOptions.setProduct(Product.Firefox);
        launchOptions.setPreferredRevision("stable_133.0");
        Browser browser1 = Puppeteer.launch(launchOptions);
        String version1 = browser1.version();
        System.out.println("webdriver bidi 协议获取到版本信息：" + version1);
        browser1.close();


        launchOptions.setProduct(Product.Chrome);
        launchOptions.setPreferredRevision("128.0.6613.137");
        Browser browser2 = Puppeteer.launch(launchOptions);
        String version2 = browser2.version();
        System.out.println("cdp协议获取到版本信息1：" + version2);
        browser2.close();

        launchOptions.setPreferredRevision("127.0.6533.99");
        Browser browser3 = Puppeteer.launch(launchOptions);
        String version3 = browser3.version();
        System.out.println("cdp协议获取到版本信息2：" + version3);
        browser3.close();
    }


    /**
     * 配置启动的浏览器版本(环境变量的版本）
     * 优先级： 4
     */
    @Test
    public void test11() throws Exception {
        launchOptions.setProduct(Product.Firefox);
        launchOptions.setHeadless(true);
        System.setProperty(Constant.JVPPETEER_PRODUCT_REVISION_ENV, "stable_133.0");
        Browser browser1 = Puppeteer.launch(launchOptions);
        String version1 = browser1.version();
        System.out.println("webdriver bidi 协议获取到版本信息1：" + version1);
        browser1.close();

        launchOptions.setProduct(Product.Chrome);
        launchOptions.setHeadless(true);
        System.setProperty(Constant.JVPPETEER_PRODUCT_REVISION_ENV, "128.0.6613.137");
        Browser browser2 = Puppeteer.launch(launchOptions);
        String version2 = browser2.version();
        System.out.println("cdp协议获取到版本信息2：" + version2);
        browser2.close();

        launchOptions.setProduct(Product.Chrome_headless_shell);
        System.setProperty(Constant.JVPPETEER_PRODUCT_REVISION_ENV, "129.0.6668.100");
        Browser browser3 = Puppeteer.launch(launchOptions);
        String version3 = browser3.version();
        System.out.println("cdp协议获取到版本信息3：" + version3);
        browser3.close();
    }

    /**
     * 不配置版本，根据Product自动扫描已经下载的最新版本
     * 优先级： 4 低
     */
    @Test
    public void test12() throws Exception {
        launchOptions.setProduct(Product.Firefox);
        Browser browser1 = Puppeteer.launch(launchOptions);
        String version1 = browser1.version();
        System.out.println("webdriver bidi 协议获取到版本信息1：" + version1);
        browser1.close();

        launchOptions.setProduct(Product.Chrome);
        launchOptions.setProtocol(Protocol.CDP);
        Browser browser2 = Puppeteer.launch(launchOptions);
        String version2 = browser2.version();
        System.out.println("cdp协议获取到版本信息2：" + version2);
        browser2.close();

        launchOptions.setProduct(Product.Chrome_headless_shell);
        launchOptions.setHeadless(true);
        Browser browser3 = Puppeteer.launch(launchOptions);
        String version3 = browser3.version();
        System.out.println("cdp协议获取到版本信息3：" + version3);
        browser3.close();
    }

    /**
     * 不做配置，扫描默认安装路径
     * 优先级： 5 低
     * {@link com.ruiyun.jvppeteer.common.Constant#PROBABLE_CHROME_EXECUTABLE_PATH}
     */
    @Test
    public void test97() throws Exception {
        Browser browser = getBrowser();
        //打开一个页面
        Page page = browser.newPage();
        Target target1 = null;
        try {
            target1 = page.target();
        } catch (UnsupportedOperationException e) {
            System.out.println("webdriver bidi 不支持 page.target() 方法");
        }
        if (Objects.nonNull(target1)) {
            System.out.println("one type=" + target1.type() + ", url=" + target1.url());
        }
        List<Target> targets = browser.targets();
        //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
        for (Target target : targets) {
            System.out.println("two type=" + target.type() + ", url=" + target.url());
        }
        browser.close();
    }

    @Test
    public void test1() throws Exception {
        //启动浏览器
        try (Browser browser = getBrowser()) {
            //打开一个页面
            Page page = browser.newPage();
            Target target1 = null;
            try {
                target1 = page.target();
            } catch (UnsupportedOperationException e) {
                System.out.println("webdriver bidi 不支持 page.target() 方法");
            }
            if (Objects.nonNull(target1)) {
                System.out.println("one type=" + target1.type() + ", url=" + target1.url());
            }
            List<Target> targets = browser.targets();
            //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
            for (Target target : targets) {
                System.out.println("two type=" + target.type() + ", url=" + target.url());
            }
        }
    }

    @Test
    public void test0() throws Exception {
        //添加启动命令行参数，这个参数使得浏览器最大化，对 chrome 起作用
        launchOptions.setArgs(Collections.singletonList("--start-maximized"));
        try (Browser browser = getBrowser()) {
            //打开一个页面
            Page page = browser.newPage();
            Target target1 = null;
            try {
                target1 = page.target();
            } catch (UnsupportedOperationException e) {
                System.out.println("webdriver bidi 不支持 page.target() 方法");
            }
            if (Objects.nonNull(target1)) {
                System.out.println("one type=" + target1.type() + ", url=" + target1.url());
            }
            List<Target> targets = browser.targets();
            //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
            for (Target target : targets) {
                System.out.println("two type=" + target.type() + ", url=" + target.url());
            }
        }
    }

    @Test
    public void test17() throws Exception {
        //指定chrome浏览器的缓存目录
        launchOptions.setCacheDir("C:\\Users\\fanyong\\Desktop\\typescriptPri\\.local-browser");
        try (Browser browser = getBrowser()) {
            //打开一个页面
            Page page = browser.newPage();
            Target target1 = null;
            try {
                target1 = page.target();
            } catch (UnsupportedOperationException e) {
                System.out.println("webdriver bidi 不支持 page.target() 方法");
            }
            if (Objects.nonNull(target1)) {
                System.out.println("one type=" + target1.type() + ", url=" + target1.url());
            }
            List<Target> targets = browser.targets();
            //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
            for (Target target : targets) {
                System.out.println("two type=" + target.type() + ", url=" + target.url());
            }
        }
    }

    @Test
    public void test18() throws Exception {
        launchOptions.setProduct(Product.Firefox);
//        launchOptions.setExecutablePath("C:\\Users\\fanyong\\Desktop\\jvppeteer\\example\\.local-browser\\win32-131.0.3\\firefox-130.0a1.en-US.win32\\firefox\\firefox.exe");
        try (Browser browser = getBrowser()) {
            //打开一个页面
            Page page = browser.newPage();
            Target target1 = page.target();
            try {
                target1 = page.target();
            } catch (UnsupportedOperationException e) {
                System.out.println("webdriver bidi 不支持 page.target() 方法");
            }
            if (Objects.nonNull(target1)) {
                System.out.println("one type=" + target1.type() + ", url=" + target1.url());
            }
            List<Target> targets = browser.targets();
            //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
            for (Target target : targets) {
                System.out.println("two type=" + target.type() + ", url=" + target.url());
            }
        }
    }

    /**
     * 测试timeout,0 代表无限等待
     *
     * @throws Exception
     */
    @Test
    public void test19() throws Exception {
        launchOptions.setProtocolTimeout(0);
        try (Browser browser = getBrowser()) {
            //打开一个页面
            Page page = browser.newPage();
            Target target1 = page.target();
            try {
                target1 = page.target();
            } catch (UnsupportedOperationException e) {
                System.out.println("webdriver bidi 不支持 page.target() 方法");
            }
            if (Objects.nonNull(target1)) {
                System.out.println("one type=" + target1.type() + ", url=" + target1.url());
            }
            List<Target> targets = browser.targets();
            //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
            for (Target target : targets) {
                System.out.println("two type=" + target.type() + ", url=" + target.url());
            }
        }
    }

    public Browser getBrowser() throws Exception {
        ArrayList<String> args = new ArrayList<>();//添加一些额外的启动参数
        args.add("--no-sandbox");
        launchOptions.setProtocolTimeout(180_000);
        launchOptions.setTimeout(180_000);
        launchOptions.setArgs(args);
        return Puppeteer.launch(launchOptions);
    }
}
