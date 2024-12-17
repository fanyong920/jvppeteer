package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.LaunchOptions;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class X_WorkingWithExtension {

    @Test
    public void test2() throws Exception {

        ArrayList<String> additionalArgs = new ArrayList<>();
        additionalArgs.add("--no-sandbox");
        additionalArgs.add("--disable-setuid-sandbox");
        //指定插件所在的文件夹,如果手上暂时没有插件，可以我个人的插件https://github.com/fanyong920/crawlItem.git 克隆下来即可
        String pathToExtension = "C:\\Users\\fanyong\\Desktop\\crawlItem-master\\crawlItem-master\\";
        additionalArgs.add("--disable-extensions-except=" + pathToExtension);
        additionalArgs.add("--load-extension=" + pathToExtension);
        //插件的加载在有头模式下才能生效
        Browser cdpBrowser = Puppeteer.launch(LaunchOptions.builder().args(additionalArgs).headless(false).build());
        List<Target> targets = cdpBrowser.targets();
        for (Target target : targets) {
            if (TargetType.BACKGROUND_PAGE.equals(target.type())) {
                System.out.println("目标类型=" + target.type());
            }
        }
        cdpBrowser.close();
    }
}
