package com.ruiyun.example;

import com.ruiyun.jvppeteer.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.OptionsBuilder;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import com.ruiyun.jvppeteer.options.ScriptTagOptions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * 在页面注入js标签
 */
public class PageAddScriptExample {
    public static void main(String[] args) throws IOException {
        // 注入jquery https://cdn.bootcss.com/jquery/3.4.1/jquery.js

        String path = new String("F:\\java教程\\49期\\vuejs\\puppeteer\\.local-chromium\\win64-722234\\chrome-win\\chrome.exe".getBytes(),"UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();

        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).withExecutablePath(path).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.setContent("<!DOCTYPE html>\n" +
                "<html>\n" +
                "\t<head>\n" +
                "\t\t<meta charset=\"UTF-8\">\n" +
                "\t\t<title></title>\n" +
                "\t\t<script type=\"text/javascript\" src=\"../js/jquery-1.11.0.js\" ></script>\n" +
                "\t\t<!--\n" +
                "\t\t\t1. 导入JQ的包\n" +
                "\t\t\t2. 文档加载完成函数: 页面初始化\n" +
                "\t\t\t3. 获得所有的行 :   元素选择器\n" +
                "\t\t\t4. 根据行号奇数/偶数去修改颜色\n" +
                "\t\t-->\n" +
                "\t\t<script>\n" +
                "\t\t\t\n" +
                "\t\t\t$(function(){\n" +
                "\t\t\t\t//获得所有的行 :   元素选择器\n" +
                "\t\t\t\t$(\"tbody > tr:even\").css(\"background-color\",\"#CCCCCC\");\n" +
                "\t\t\t\t//修改基数行\n" +
                "\t\t\t\t$(\"tbody > tr:odd\").css(\"background-color\",\"#FFF38F\");\n" +
                "//\t\t\t\t$(\"tbody > tr\").css(\"background-color\",\"#FFF38F\");\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t});\n" +
                "\t\t</script>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<table border=\"1px\" width=\"600px\" id=\"tab\">\n" +
                "\t\t\t<thead>\n" +
                "\t\t\t\t<tr >\n" +
                "\t\t\t\t\t<td>\n" +
                "\t\t\t\t\t\t<input type=\"checkbox\" />\n" +
                "\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t<td>分类ID</td>\n" +
                "\t\t\t\t\t<td>分类名称</td>\n" +
                "\t\t\t\t\t<td>分类商品</td>\n" +
                "\t\t\t\t\t<td>分类描述</td>\n" +
                "\t\t\t\t\t<td>操作</td>\n" +
                "\t\t\t\t</tr>\n" +
                "\t\t\t</thead>\n" +
                "\t\t\t<tbody>\n" +
                "\t\t\t\t<tr>\n" +
                "\t\t\t\t<td>\n" +
                "\t\t\t\t\t<input type=\"checkbox\" />\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td>1</td>\n" +
                "\t\t\t\t<td>手机数码</td>\n" +
                "\t\t\t\t<td>华为,小米,尼康</td>\n" +
                "\t\t\t\t<td>黑马数码产品质量最好</td>\n" +
                "\t\t\t\t<td>\n" +
                "\t\t\t\t\t<a href=\"#\">修改</a>|<a href=\"#\">删除</a>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td>\n" +
                "\t\t\t\t\t<input type=\"checkbox\" />\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td>2</td>\n" +
                "\t\t\t\t<td>成人用品</td>\n" +
                "\t\t\t\t<td>充气的</td>\n" +
                "\t\t\t\t<td>这里面的充气电动硅胶的</td>\n" +
                "\t\t\t\t<td><a href=\"#\">修改</a>|<a href=\"#\">删除</a></td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td>\n" +
                "\t\t\t\t\t<input type=\"checkbox\" />\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td>3</td>\n" +
                "\t\t\t\t<td>电脑办公</td>\n" +
                "\t\t\t\t<td>联想,小米</td>\n" +
                "\t\t\t\t<td>笔记本特卖</td>\n" +
                "\t\t\t\t<td><a href=\"#\">修改</a>|<a href=\"#\">删除</a></td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td>\n" +
                "\t\t\t\t\t<input type=\"checkbox\" />\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td>4</td>\n" +
                "\t\t\t\t<td>馋嘴零食</td>\n" +
                "\t\t\t\t<td>辣条,麻花,黄瓜</td>\n" +
                "\t\t\t\t<td>年货</td>\n" +
                "\t\t\t\t<td><a href=\"#\">修改</a>|<a href=\"#\">删除</a></td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td>\n" +
                "\t\t\t\t\t<input type=\"checkbox\" />\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td>5</td>\n" +
                "\t\t\t\t<td>床上用品</td>\n" +
                "\t\t\t\t<td>床单,被套,四件套</td>\n" +
                "\t\t\t\t<td>都是套子</td>\n" +
                "\t\t\t\t<td><a href=\"#\">修改</a>|<a href=\"#\">删除</a></td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t</tbody>\n" +
                "\t\t</table>\n" +
                "\t\t\n" +
                "\t</body>\n" +
                "</html>\n");

        page.addScriptTag(new ScriptTagOptions("https://cdn.bootcss.com/jquery/3.4.1/jquery.js"));
    }
}
