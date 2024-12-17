package com.ruiyun.example;

import com.ruiyun.jvppeteer.cdp.entities.Token;
import com.ruiyun.jvppeteer.util.QueryHandlerUtil;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test2 {
    public static void main(String[] args) throws Exception {
//        System.out.println(PrimitiveValue.class);
//        try (Browser browser = Puppeteer.launch()) {
//            //打开一个页面
//            Page page = browser.newPage();
//            Target target1 = page.target();
//            System.out.println("one type=" + target1.type() + ", url=" + target1.url());
//            List<Target> targets = browser.targets();
//            //看看targets里面都有什么，包含browser,page,等类型,其中还包含了上面newPage得到page
//            for (Target target : targets) {
//                System.out.println("two type=" + target.type() + ", url=" + target.url());
//            }
//        }
        List<Token> tokens = QueryHandlerUtil.tokenize("#vqfcts-501 > div > div:nth-child(1) > div > div.lc_expfact_default");
        System.out.println(tokens.size());
        System.out.println(Arrays.deepToString(tokens.toArray()));
        final Pattern pseudo_class = Pattern.compile(":(?<name>[-\\w\\P{ASCII}]+)(?:\\((?<argument>¶*)\\))?", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pseudo_class.matcher(":nth-child(1)");
        System.out.println(matcher.matches());
    }
}
