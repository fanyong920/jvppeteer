package com.ruiyun.example;

import com.ruiyun.jvppeteer.core.Browser;
import com.ruiyun.jvppeteer.core.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.Target;
import java.util.List;

public class Test2 {
    public static void main(String[] args) throws Exception {
        try (Browser browser = Puppeteer.launch()) {
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
    }
}
