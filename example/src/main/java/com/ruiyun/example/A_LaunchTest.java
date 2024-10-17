package com.ruiyun.example;

import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Target;
import com.ruiyun.jvppeteer.entities.LaunchOptions;
import com.ruiyun.jvppeteer.entities.LaunchOptionsBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class A_LaunchTest {
    public final LaunchOptions launchOptions = new LaunchOptionsBuilder().
            //有界面模式 true未无界面
                    withHeadless(false).withDebuggingPort(9222)
            //手动配置chrome执行路径
            //.withExecutablePath("C:\\Users\\fanyong\\Desktop\\chrome-win-131\\chrome-win\\chrome.exe")
            //.withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").withDebuggingPort(9222)
            .build();

    /**
     * 手动配置路径来启动浏览器
     * 优先级： 1 高
     */
    @Test
    public void test99() throws InterruptedException, IOException {
        launchOptions.setExecutablePath("C:\\Users\\fanyong\\Desktop\\chrome-win-131\\chrome-win\\chrome.exe");
        Browser browser = getBrowser();
        //打开一个页面
        Page page = browser.newPage();
        Target target1 = page.target();
        System.out.println("one type=" + target1.type() + ", url=" + target1.url() + ",id=" + target1.getTargetId());
        List<Target> targets = browser.targets();
        //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
        for (Target target : targets) {
            System.out.println("two type=" + target.type() + ", url=" + target.url() + ",id=" + target.getTargetId());
        }
        Thread.sleep(10000);
    }

    /**
     * 将浏览器启动路径写进环境变量中启动
     * 优先级： 2
     * {@link com.ruiyun.jvppeteer.common.Constant#EXECUTABLE_ENV}
     */
    @Test
    public void test98() throws InterruptedException, IOException {
        System.setProperty("JVPPETEER_EXECUTABLE_PATH", "C:\\Users\\fanyong\\Desktop\\jvppeteer\\example\\.local-browser\\win32-127.0.6533.99\\chrome-win32\\chrome.exe");
        //System.setProperty("java_config_jvppeteer_executable_path", "C:\\Users\\fanyong\\Desktop\\jvppeteer\\example\\.local-browser\\win32-127.0.6533.99\\chrome-win32\\chrome.exe");
        //System.setProperty("java_package_config_jvppeteer_executable_path", "C:\\Users\\fanyong\\Desktop\\jvppeteer\\example\\.local-browser\\win32-127.0.6533.99\\chrome-win32\\chrome.exe");
        Browser browser = getBrowser();
        //打开一个页面
        Page page = browser.newPage();
        Target target1 = page.target();
        System.out.println("one type=" + target1.type() + ", url=" + target1.url() + ",id=" + target1.getTargetId());
        List<Target> targets = browser.targets();
        //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
        for (Target target : targets) {
            System.out.println("two type=" + target.type() + ", url=" + target.url() + ",id=" + target.getTargetId());
        }
        Thread.sleep(10000);
    }

    /**
     * 配置启动的浏览器版本(首选版本）
     * 优先级： 3
     */
    @Test
    public void test10() throws IOException {
        launchOptions.setProduct(Product.CHROME);
        launchOptions.setPreferredRevision("128.0.6613.137");
        Browser browser = Puppeteer.launch(launchOptions);
        String version = browser.version();
        System.out.println("cdp协议获取到版本信息1：" + version);
        browser.close();

        launchOptions.setPreferredRevision("127.0.6533.99");
        Browser browser2 = Puppeteer.launch(launchOptions);
        String version2 = browser2.version();
        System.out.println("cdp协议获取到版本信息2：" + version2);
        browser2.close();
    }


    /**
     * 配置启动的浏览器版本(环境变量的版本）
     * 优先级： 4
     */
    @Test
    public void test11() throws InterruptedException, IOException {
        launchOptions.setProduct(Product.CHROME);
        launchOptions.setHeadless(true);
        System.setProperty("JVPPETEER_CHROMIUM_REVISION", "128.0.6613.137");
        Browser browser = Puppeteer.launch(launchOptions);
        String version = browser.version();
        System.out.println("cdp协议获取到版本信息1：" + version);
        browser.close();

        launchOptions.setProduct(Product.CHROMEHEADLESSSHELL);
        System.setProperty("JVPPETEER_CHROMIUM_REVISION", "129.0.6668.100");
        Browser browser2 = Puppeteer.launch(launchOptions);
        String version2 = browser2.version();
        System.out.println("cdp协议获取到版本信息2：" + version2);
        browser2.close();
    }

    /**
     * 不配置版本，根据Product自动扫描已经下载的最新版本
     * 优先级： 4 低
     */
    @Test
    public void test12() throws InterruptedException, IOException {

        launchOptions.setProduct(Product.CHROME);
        Browser browser = Puppeteer.launch(launchOptions);
        String version = browser.version();
        System.out.println("cdp协议获取到版本信息1：" + version);
        browser.close();

        launchOptions.setProduct(Product.CHROMEHEADLESSSHELL);
        launchOptions.setHeadless(true);
        Browser browser2 = Puppeteer.launch(launchOptions);
        String version2 = browser2.version();
        System.out.println("cdp协议获取到版本信息2：" + version2);
        browser2.close();

        //CHROMEDRIVER不适应程序的启动逻辑，这里只是验证是否能正确找到浏览器版本
        launchOptions.setProduct(Product.CHROMEDRIVER);
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
    public void test97() throws InterruptedException, IOException {
        Browser browser = getBrowser();
        //打开一个页面
        Page page = browser.newPage();
        Target target1 = page.target();
        System.out.println("one type=" + target1.type() + ", url=" + target1.url() + ",id=" + target1.getTargetId());
        List<Target> targets = browser.targets();
        //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
        for (Target target : targets) {
            System.out.println("two type=" + target.type() + ", url=" + target.url() + ",id=" + target.getTargetId());
        }
        Thread.sleep(10000);
    }

    @Test
    public void test1() throws IOException {
        //启动浏览器
        Browser browser = getBrowser();
        //打开一个页面
        Page page = browser.newPage();
        Target target1 = page.target();
        System.out.println("one type=" + target1.type() + ", url=" + target1.url() + ",id=" + target1.getTargetId());
        List<Target> targets = browser.targets();
        //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
        for (Target target : targets) {
            System.out.println("two type=" + target.type() + ", url=" + target.url() + ",id=" + target.getTargetId());
        }
    }

    @Test
    public void test0() throws InterruptedException, IOException {
        //添加启动命令行参数，这个参数使得浏览器最大化
        launchOptions.setPipe(true);
        launchOptions.setArgs(Collections.singletonList("--start-maximized"));
        Browser browser = getBrowser();
        //打开一个页面
        Page page = browser.newPage();
        Target target1 = page.target();
        System.out.println("one type=" + target1.type() + ", url=" + target1.url() + ",id=" + target1.getTargetId());
        List<Target> targets = browser.targets();
        //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
        for (Target target : targets) {
            System.out.println("two type=" + target.type() + ", url=" + target.url() + ",id=" + target.getTargetId());
        }
        Thread.sleep(10000);
    }

    public Browser getBrowser() throws IOException {
        return Puppeteer.launch(launchOptions);
    }


}
