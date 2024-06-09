package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 除了关闭dialog，还可以关闭弹出的页面（page）
 */
public class PageDialogExample {
    public static void main(String[] args) throws Exception {

        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        //监听alert事件，监听到后dismiss它，然后关闭浏览器
        page.goTo("https://zhuanlan.zhihu.com/p/97564816");

        //这里是关闭dialog
        page.onDialog(dialog -> {
            dialog.dismiss();
            System.out.println(dialog.type());
        });

        //这里模拟关闭弹出的页面
        Thread.sleep(5000L);//这个时候我手动点击页面上的分享，模拟弹出页面
        try {
            List<Target> targets = browser.targets();
            targets.forEach(item -> {
                System.out.println(item.getTargetInfo().getType());
                String title = item.getTargetInfo().getTitle();
                System.out.println(title);
                if(title.contains("分享到微博")) {
                    try {
                        item.page().close();
                        System.out.println("哈哈，关闭了");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
//        page.evaluate("() => alert('1')", PageEvaluateType.FUNCTION);
    }
}
