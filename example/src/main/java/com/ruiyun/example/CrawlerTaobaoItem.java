package com.ruiyun.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlerTaobaoItem {
    public static void main(String[] args) throws Exception {
        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);
        //指定启动路径，启动浏览器
        ArrayList<String> argList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(argList).withHeadless(false).build();
        argList.add("--no-sandbox");
        argList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);

        //启动一个线程池多线程抓取
        int threadCount = 5;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(threadCount, threadCount, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
        CompletionService service = new ExecutorCompletionService(executor);
        //打开5个页面同时抓取，这些页面可以多次利用，这样减少创建网页带来的性能消耗
        LinkedBlockingQueue<Page> pages = new LinkedBlockingQueue<>();
        for (int i = 0; i < threadCount; i++) {
            Page page = browser.newPage();
            //拦截请求,可选，但是存在线程切换比较严重，建议不拦截
//            page.onRequest(request -> {
//                if ("image".equals(request.resourceType()) || "media".equals(request.resourceType())) {
//                    //遇到多媒体或者图片资源请求，拒绝，加载页面加载
//                    request.abort();
//                } else {//其他资源放行
//                    request.continueRequest();
//                }
//            });
//            page.setRequestInterception(true);
            pages.put(page);//往队列后面放,阻塞
        }
        //结果集
        List<Future<Object>> futures = new ArrayList<>();
        //抓取100次
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Future<Object> future = service.submit(new CrawlerCallable(pages));
            futures.add(future);
        }

        //关闭线程池
        executor.shutdown();
        //获取结果
        int i = 0;
        for (Future<Object> result : futures) {
            Object item = result.get();
            i++;
            System.out.println(i + ":" + Constant.OBJECTMAPPER.writeValueAsString(item));
        }
        long end = System.currentTimeMillis();
        System.out.println("时间：" + (end - start));
    }

    static class CrawlerCallable implements Callable<Object> {

        private LinkedBlockingQueue<Page> pages;

        public CrawlerCallable(LinkedBlockingQueue<Page> pages) {
            this.pages = pages;
        }

        @Override
        public Object call() {
            Page page = null;
            try {
                page = pages.take();
                PageNavigateOptions navigateOptions = new PageNavigateOptions();
                //如果不设置 domcontentloaded 算页面导航完成的话，那么goTo方法会超时，因为图片请求被拦截了，页面不会达到loaded阶段
                navigateOptions.setWaitUntil(Collections.singletonList("domcontentloaded"));
                page.goTo("https://item.taobao.com/item.htm?id=541605195654", navigateOptions);
                String content = page.content();
                return parseItem(content);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (page != null) {
                    try {
                        pages.put(page);//把已经抓取完的网页放回队列里
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        private Object parseItem(String content) throws ParserException {
            String isExists = "true";    //判断商品是否删除或下架
            String title = null;        //商品标题
            String userType = null;        //商品所属用户类别
            String nick = null;            //商品所属nick
            String shopName = null;        //商品所属店铺名称
            String cid = null;            //商品所属系统类目
            String picUrl = null;        //商品主图URL

            String descUrl = null;        //商品PC描述URL
            String skuMapStr = null;    //商品sku信息
            String subTitle = null;        //商品卖点
            String cccNum = null;        //CCC证书编号
            String induCerCode = null;  //工业产品许可码
            String isSecond = "false";    //是否二手商品
            String isGlobal = "false";    //是否代购商品
            String isCustomerMade = "false";    //是否定制商品
            String itemImgStr = "";//商品图片
            String skuWebDataStr = "";//销售属性pv-页面上的数据map(天猫/集市)
            String catPropList = "";//天猫国际的非销售属性数据
            String price = "";//一口价
            String location = "";//商品所在地
            String sellerCids = "";//自定义分类
            String spuId = "";//产品id
            String masterVideoUrl = "";//主图视频url
            String qualiImgUrl = "";//资质图url
            String foodAddCer = "";//食品添加剂生产许可证编号

            // 一、解析商品主要信息
            StringBuilder resBuffer = new StringBuilder(content);
            /**1、商品标题、用户类别*/
            Pattern pattern = Pattern.compile("<title>.*?</title>");
            Matcher matcher = pattern.matcher(resBuffer.toString());
            String titleText = null;
            if (matcher.find()) {
                titleText = matcher.group();
            }
            if (StringUtil.isNotEmpty(titleText)) {
                title = titleText.replace("<title>", "").replace("</title>", "").trim();
                if (title.contains("-淘宝网")) {
                    title = title.replace("-淘宝网", "");
                    userType = "C";
                } else if (title.contains("-tmall.com天猫")) {
                    title = title.replace("-tmall.com天猫", "");
                    userType = "B";
                }
            } else {
                pattern = Pattern.compile("<input type=\"hidden\" name=\"title\" value=\".*?\" />");
                matcher = pattern.matcher(resBuffer.toString());
                if (matcher.find()) {
                    titleText = matcher.group();
                }
                if (StringUtil.isNotEmpty(titleText)) {
                    title = titleText.replace("<input type=\"hidden\" name=\"title\" value=\"", "").replace("\" />", "").trim();
                }

                //判断店铺类型 <span class=\"mlogo\"><a href=\"//www.tmall.com/\" title=\"天猫Tmall.com\">
                pattern = Pattern.compile(" <span class=\"mlogo\"><a href=\"//www.tmall.com/\" title=\"天猫Tmall.com\">");
                matcher = pattern.matcher(resBuffer.toString());
                if (matcher.find()) {
                    userType = "B";
                } else {
                    userType = "C";
                }

            }

            /**2、用户昵称*/
            if ("C".equals(userType)) {
                pattern = Pattern.compile("sellerNick       : '.*?',");
                matcher = pattern.matcher(resBuffer.toString());
                String nickText = null;
                if (matcher.find()) {
                    nickText = matcher.group();
                }
                if (StringUtil.isNotEmpty(nickText)) {
                    nick = nickText.replace("sellerNick       : '", "").replace("',", "").trim();
                }
            } else if ("B".equals(userType)) {
                pattern = Pattern.compile("<input type=\"hidden\" name=\"seller_nickname\" value=\".*?\" />");
                matcher = pattern.matcher(resBuffer.toString());
                String nickText = null;
                if (matcher.find()) {
                    nickText = matcher.group();
                }
                if (StringUtil.isNotEmpty(nickText)) {
                    nick = nickText.replace("<input type=\"hidden\" name=\"seller_nickname\" value=\"", "").replace("\" />", "").trim();
                }
            }

            /**3、店铺名称*/
            if ("C".equals(userType)) {
                pattern = Pattern.compile("<a href=\"//[^>]*?.taobao.com\" title=\".*?\" target=\"_blank\">");
                matcher = pattern.matcher(resBuffer.toString());
                String shopNameText = null;
                if (matcher.find()) {
                    shopNameText = matcher.group();
                }
                if (StringUtil.isNotEmpty(shopNameText)) {
                    int start = shopNameText.indexOf("title=\"");
                    int end = shopNameText.indexOf("\" target=\"_blank");
                    shopName = shopNameText.substring(start + 7, end);
                }
            } else if ("B".equals(userType)) {
                pattern = Pattern.compile("<a class=\"slogo-shopname\" href=\"//[^>]*?.tmall.com\" data-spm=\"[^>]*?\"><strong>.*?</strong></a>");
                matcher = pattern.matcher(resBuffer.toString());
                String shopNameText = null;
                if (matcher.find()) {
                    shopNameText = matcher.group();
                }
                if (StringUtil.isNotEmpty(shopNameText)) {
                    int start = shopNameText.indexOf("><strong>");
                    int end = shopNameText.indexOf("</strong></a>");
                    shopName = shopNameText.substring(start + 9, end);
                }
            }
            if (StringUtil.isEmpty(shopName)) {   //bug:36332
                pattern = Pattern.compile("shopName         : '.*?',");
                matcher = pattern.matcher(resBuffer.toString());
                String shopNameText = null;
                if (matcher.find()) {
                    shopNameText = matcher.group();
                }
                if (StringUtil.isNotEmpty(shopNameText)) {
                    shopNameText = shopNameText.replace("shopName         : '", "").replace("',", "").trim();
                    shopName = shopNameText;
                }
            }

            /**4、系统类目*/
            pattern = Pattern.compile("category=item%5f.*?&userid");
            matcher = pattern.matcher(resBuffer.toString());
            String cidText = null;
            if (matcher.find()) {
                cidText = matcher.group();
            }
            if (StringUtil.isNotEmpty(cidText)) {
                cid = cidText.replace("category=item%5f", "").replace("&userid", "").trim();
            }

            /**5、主图URL*/
            if ("C".equals(userType)) {
                pattern = Pattern.compile("<a href=\"#\"><img data-src=\".*?_50x50.jpg\" /></a>");
                matcher = pattern.matcher(resBuffer.toString());
                String picUrlText = null;
                if (matcher.find()) {
                    picUrlText = matcher.group();
                }
                if (StringUtil.isNotEmpty(picUrlText)) {
                    picUrl = picUrlText.replace("<a href=\"#\"><img data-src=\"", "").replace("_50x50.jpg\" /></a>", "").trim();
                    if (picUrl.startsWith("//")) {
                        picUrl = "http:" + picUrl;
                    }
                }
            } else if ("B".equals(userType)) {
                pattern = Pattern.compile("<a href=\"#\"><img src=\".*?_60x60q90.jpg\" /></a>");
                matcher = pattern.matcher(resBuffer.toString());
                String picUrlText = null;
                if (matcher.find()) {
                    picUrlText = matcher.group();
                }
                if (StringUtil.isNotEmpty(picUrlText)) {
                    picUrl = picUrlText.replace("<a href=\"#\"><img src=\"", "").replace("_60x60q90.jpg\" /></a>", "").trim();
                    if (picUrl.startsWith("//")) {
                        picUrl = "http:" + picUrl;
                    }
                }
            }


            // 二、解析商品附加信息

            Parser parser = new Parser(resBuffer.toString());
            PrototypicalNodeFactory p = new PrototypicalNodeFactory();
            p.registerTag(new ScriptTag());
            parser.setNodeFactory(p);

            NodeFilter filterJS = new NodeClassFilter(ScriptTag.class);
            NodeList nodelistJS = null;
            nodelistJS = parser.extractAllNodesThatMatch(filterJS);
            /**A、商品PC描述URL */
            for (int i = 0; i < nodelistJS.size(); i++) {
                Node textnode = (Node) nodelistJS.elementAt(i);
                if (textnode.toString().contains("dsc.taobaocdn.com")) {
                    boolean flag = true;
                    //集市商品
                    Pattern pa = Pattern.compile("dsc.taobaocdn.com(.*?)\' :");
                    Matcher m = pa.matcher(textnode.toString());
                    while (m.find()) {
                        descUrl = m.group().substring(0, m.group().length() - 3);
                        flag = false;
                        break;
                    }
                    if (flag) {//天猫商品
                        pa = Pattern.compile("dsc.taobaocdn.com(.*?)\"");
                        m = pa.matcher(textnode.toString());
                        while (m.find()) {
                            descUrl = m.group().substring(0, m.group().length() - 1);
                            break;
                        }
                    }
                    break;
                } else if (textnode.toString().contains("desc.alicdn.com")) {
                    boolean flag = true;
                    //集市商品
                    Pattern pa = Pattern.compile("desc.alicdn.com(.*?)\'");
                    Matcher m = pa.matcher(textnode.toString());
                    while (m.find()) {
                        descUrl = m.group().substring(0, m.group().length() - 1);
                        flag = false;
                        break;
                    }
                    if (flag) {//天猫商品
                        pa = Pattern.compile("desc.alicdn.com(.*?)\"");
                        m = pa.matcher(textnode.toString());
                        while (m.find()) {
                            descUrl = m.group().substring(0, m.group().length() - 1);
                            break;
                        }
                    }
                    break;
                } else if (textnode.toString().contains("\"tfsUrl\":\"")) {
                    //bug28736:飞猪商品
                    Pattern pa = Pattern.compile("\"tfsUrl\":\".*?\"");
                    Matcher m = pa.matcher(textnode.toString());
                    if (m.find()) {
                        descUrl = m.group().replace("\"tfsUrl\":\"", "");
                        descUrl = descUrl.substring(0, descUrl.length() - 1);
                        if (descUrl.startsWith("//")) {
                            descUrl = descUrl.substring(2, descUrl.length());
                        }
                    }
                } else if (textnode.toString().contains("dscnew.taobao.com")) {
                    boolean flag = true;
                    //集市商品
                    Pattern pa = Pattern.compile("dscnew.taobao.com(.*?)\'");
                    Matcher m = pa.matcher(textnode.toString());
                    while (m.find()) {
                        descUrl = m.group().substring(0, m.group().length() - 1);
                        flag = false;
                        break;
                    }
                    if (flag) {//天猫商品
                        pa = Pattern.compile("dscnew.taobao.com(.*?)\"");
                        m = pa.matcher(textnode.toString());
                        while (m.find()) {
                            descUrl = m.group().substring(0, m.group().length() - 1);
                            break;
                        }
                    }
                    break;
                }
            }

            /**B、商品sku信息 */
            for (int i = 0; i < nodelistJS.size(); i++) {
                Node textnode = (Node) nodelistJS.elementAt(i);
                if (textnode.toString().contains("skuMap")) {
                    boolean flag = true;
                    //集市商品
                    Pattern pa = Pattern.compile("skuMap     : (.*?),propertyMemoMap");
                    Matcher m = pa.matcher(textnode.toString());
                    if (m.find()) {
                        skuMapStr = m.group().substring(0, m.group().length() - 1);
                        flag = false;
                    }
                    if (flag) {//天猫商品
                        pa = Pattern.compile("skuMap\":(.*?),\"valLoginIndicator");
                        m = pa.matcher(textnode.toString());
                        if (m.find()) {
                            skuMapStr = m.group().substring(0, m.group().length() - 1);
                        }
                    }
                    break;
                }
            }
            if (skuMapStr != null) {
                if (skuMapStr.indexOf("skuMap\":") > -1) {    //天猫
                    int start = skuMapStr.indexOf("{");
                    int end = skuMapStr.indexOf(",\"valLoginIndicato");
                    skuMapStr = skuMapStr.substring(start, end);
                } else if (skuMapStr.indexOf("skuMap     : ") > -1) {    //集市
                    int start = skuMapStr.indexOf("{");
                    int end = skuMapStr.indexOf("            ,propertyMemoMa");
                    skuMapStr = skuMapStr.substring(start, end);
                }
                Map<String, String> pvDataMap = new HashMap<>();//销售属性pv-页面上的数据map(天猫/集市)
                Pattern pa = Pattern.compile("<li\\s*?data-value=(.|\n)*?</li>");
                Matcher m = pa.matcher(resBuffer.toString());
                while (m.find()) {
                    String pvWebData = m.group();
                    Pattern pat = Pattern.compile("data-value=\"\\d+?:\\d+?\"");
                    Matcher ma = pat.matcher(pvWebData);
                    if (ma.find()) {
                        String pv = ma.group();
                        pv = pv.replace("data-value=\"", "");
                        pv = pv.replace("\"", "").trim();
                        pvDataMap.put(pv, pvWebData);
                    }
                }
                if (pvDataMap.size() > 0) {
                    try {
                        skuWebDataStr = Constant.OBJECTMAPPER.writeValueAsString(pvDataMap);
                    } catch (Exception e) {

                    }
                }
            }

            /**C、商品卖点 */
            Pattern start = Pattern.compile("<p class=\"tb-subtitle\">.*?</p>");
            Matcher ms = start.matcher(resBuffer.toString());
            String subTitleText = null;

            if (ms.find()) {
                subTitleText = ms.group();
            }
            if (StringUtil.isNotEmpty(subTitleText)) {
                subTitle = subTitleText.replace("<p class=\"tb-subtitle\">", "").replace("</p>", "").trim();
            }

            /**D、资质编号 */
            start = Pattern.compile("wd=.*&ac=29");
            ms = start.matcher(resBuffer.toString());
            String cccText = null;
            if (ms.find()) {
                cccText = ms.group();
            }
            if (StringUtil.isNotEmpty(cccText)) {
                cccNum = cccText.replace("wd=", "").replace("&ac=29", "").trim();
            } else {
                //天猫商品
                start = Pattern.compile("<li title=.*>证书编号：");
                ms = start.matcher(resBuffer.toString());
                subTitleText = null;
                if (ms.find()) {
                    cccText = ms.group();
                }
                if (StringUtil.isNotEmpty(cccText)) {
                    cccNum = cccText.replace("<li title=\"", "").replace("\">证书编号：", "").trim();
                }
            }

            /**E、二手商品标记 */
            start = Pattern.compile("<span class=\"tb-stuff-status\">二手</span>");
            ms = start.matcher(resBuffer.toString());
            if (ms.find()) {
                isSecond = "true";
            } else {
                isSecond = "false";
            }

            /**F、代购商品标记 */
            start = Pattern.compile("<span class=\"tb-stuff-status\">代购</span>");
            ms = start.matcher(resBuffer.toString());
            if (ms.find()) {
                isGlobal = "true";
            } else {
                start = Pattern.compile("isInternational: .*?,pageType");
                ms = start.matcher(resBuffer.toString());
                if (ms.find() && ms.group().contains("true")) {
                    isGlobal = "true";
                } else {
                    isGlobal = "false";
                }
            }

            //定制
            start = Pattern.compile("<span class=\"tb-stuff-status\">定制</span>");
            ms = start.matcher(resBuffer.toString());
            if (ms.find()) {
                isCustomerMade = "true";
            } else {
                isCustomerMade = "false";
            }

            //判断商品平台

            start = Pattern.compile("<title>.*?</title>");
            ms = start.matcher(resBuffer.toString());
            if (ms.find()) {
                titleText = ms.group();
            }
            if (StringUtil.isNotEmpty(titleText)) {
                title = titleText.replace("<title>", "").replace("</title>", "").trim();
                if (title.contains("-淘宝网")) {
                    title = title.replace("-淘宝网", "");
                    userType = "C";
                } else if (title.contains("-tmall.com天猫")) {
                    title = title.replace("-tmall.com天猫", "");
                    userType = "B";
                }
            }
            //天猫商品获取主图信息
            /**5、主图URL*/
            if ("C".equals(userType)) {
                start = Pattern.compile("<a href=\"#\"><img data-src=\".*?_50x50.jpg\" /></a>");
                ms = start.matcher(resBuffer.toString());
                List<String> itemImgs = new ArrayList<>();
                while (ms.find()) {
                    String imgUrl = ms.group();
                    if (StringUtil.isNotEmpty(imgUrl)) {
                        imgUrl = imgUrl.replace("<a href=\"#\"><img data-src=\"", "").replace("_50x50.jpg\" /></a>", "").trim();
                    }
                    if (imgUrl.startsWith("//")) {
                        imgUrl = "http:" + imgUrl;
                    }
                    itemImgs.add(imgUrl);
                }
                if (itemImgs.size() > 0) {
                    try {
                        itemImgStr = Constant.OBJECTMAPPER.writeValueAsString(itemImgs);
                    } catch (Exception e) {

                    }
                }
            } else if ("B".equals(userType)) {
                start = Pattern.compile("<a href=\"#\"><img src=\".*?_60x60q90.jpg\" /></a>");
                ms = start.matcher(resBuffer.toString());
                List<String> itemImgs = new ArrayList<>();
                while (ms.find()) {
                    String imgUrl = ms.group();
                    if (StringUtil.isNotEmpty(imgUrl)) {
                        imgUrl = imgUrl.replace("<a href=\"#\"><img src=\"", "").replace("_60x60q90.jpg\" /></a>", "").trim();
                    }
                    if (imgUrl.startsWith("//")) {
                        imgUrl = "http:" + imgUrl;
                    }
                    itemImgs.add(imgUrl);
                }
                if (itemImgs.size() > 0) {
                    try {
                        itemImgStr = Constant.OBJECTMAPPER.writeValueAsString(itemImgs);
                    } catch (Exception e) {

                    }
                }
            }

            //天猫国际的非销售属性
            start = Pattern.compile("\"catPropList\":\\[.+?\\],\"foodProDate\"");
            ms = start.matcher(resBuffer.toString());
            if (ms.find()) {
                String matchText = ms.group();
                matchText = matchText.replace("\"catPropList\":", "").replace(",\"foodProDate\"", "");
                if (StringUtil.isNotEmpty(matchText)) {
                    catPropList = matchText;
                }
            }

            //bug17021:工业产品许可码
            if ("C".equals(userType)) {
                start = Pattern.compile("商品具有：<a href=\"//baike.taobao.com/list.htm\\?q=.*?\".*?>工业产品生产许可证号</a>");
                ms = start.matcher(resBuffer.toString());
                if (ms.find()) {
                    String matchText = ms.group();
                    matchText = matchText.replace("商品具有：<a href=\"//baike.taobao.com/list.htm?q=", "")
                            .replaceAll("\".*?>工业产品生产许可证号</a>", "");
                    if (StringUtil.isNotEmpty(matchText)) {
                        induCerCode = matchText;
                    }
                }
            } else if ("B".equals(userType)) {
                //bug17550:天猫商品的工业生产许可证号获取
                //start = Pattern.compile("商品具有：<a href=\"//baike.taobao.com/view.htm\\?wd=.*?&amp;ac=38\".*?>工业生产许可证号</a>");
                //bug17875:正则表达式修改
                start = Pattern.compile("商品具有：<a href=\"//baike.taobao.com/view.htm\\?((?!>).)*?wd=((?!>).)*?>工业生产许可证号</a>");
                ms = start.matcher(resBuffer.toString());
                if (ms.find()) {
                    String matchText = ms.group();
                    matchText = matchText.replaceAll("商品具有：<a href=\"//baike.taobao.com/view.htm\\?((?!>).)*?wd=", "")
                            .replaceAll("(&amp;|\")((?!>).)*?>工业生产许可证号</a>", "");
                    if (StringUtil.isNotEmpty(matchText)) {
                        try {
                            induCerCode = URLDecoder.decode(matchText, "GBK");
                        } catch (Exception e) {

                        }
                    }
                }
            }

            //一口价
            if ("C".equals(userType)) {
                start = Pattern.compile("<input type=\"hidden\" name=\"current_price\" value= \".*?\"/>");
                ms = start.matcher(resBuffer.toString());
                if (ms.find()) {
                    String matchText = ms.group();
                    matchText = matchText.replace("<input type=\"hidden\" name=\"current_price\" value= \"", "")
                            .replace("\"/>", "");

                    price = matchText;

                }
            } else if ("B".equals(userType)) {
                start = Pattern.compile(",\"defaultItemPrice\":\".*?\",");
                ms = start.matcher(resBuffer.toString());
                if (ms.find()) {
                    String matchText = ms.group();
                    matchText = matchText.replace(",\"defaultItemPrice\":\"", "");
                    matchText = matchText.substring(0, matchText.length() - 2);
                    Pattern pa = Pattern.compile("\\d+(\\.\\d+)?");
                    Matcher ma = pa.matcher(matchText);
                    if (ma.find()) {
                        String priceStr = ma.group();

                        price = priceStr;

                    }
                }
            }
            if ("C".equals(userType)) {
                start = Pattern.compile("<meta name=\"keywords\" content=\".*?\"/>");
                ms = start.matcher(resBuffer.toString());
                if (ms.find()) {
                    String matchText = ms.group();
                    matchText = matchText.replace("<meta name=\"keywords\" content=\"", "")
                            .replace("\"/>", "");
                    matchText = matchText.trim();
                    if (StringUtil.isNotEmpty(matchText)) {
                        String[] split = matchText.split(" ");
                        if (split != null && split.length > 0 && StringUtil.isNotEmpty(split[split.length - 1])) {
                            location = split[split.length - 1];
                        }
                    }
                }
            } else if ("B".equals(userType)) {
                String zipStr = resBuffer.toString().replace(" ", "");
                start = Pattern.compile("<label>所在地：</label><divclass=\"right\">.*?</div>");
                ms = start.matcher(zipStr);
                if (ms.find()) {
                    String matchText = ms.group();
                    matchText = matchText.replace("<label>所在地：</label><divclass=\"right\">", "").replace("</div>", "");
                    if (StringUtil.isNotEmpty(matchText)) {
                        location = matchText;
                    }
                }
            }

            start = Pattern.compile("category=item%5f.*?&userid");
            ms = start.matcher(resBuffer.toString());

            if (ms.find()) {
                cidText = ms.group();
            }
            if (StringUtil.isNotEmpty(cidText)) {
                cid = cidText.replace("category=item%5f", "").replace("&userid", "").trim();
            }

            //自定义分类
            if ("C".equals(userType)) {
                start = Pattern.compile("rstShopcatlist: '.*?'");
                ms = start.matcher(resBuffer.toString());
                if (ms.find()) {
                    String matchText = ms.group();
                    matchText = matchText.replace("rstShopcatlist: '", "");
                    matchText = matchText.substring(0, matchText.length() - 1);
                    if (StringUtil.isNotEmpty(matchText)) {
                        sellerCids = matchText;
                    }
                }
            }

            //所属产品id
            if ("B".equals(userType)) {
                start = Pattern.compile("\"spuId\":\"\\d+?\",");
                ms = start.matcher(resBuffer.toString());
                if (ms.find()) {
                    String matchText = ms.group();
                    matchText = matchText.replace("\"spuId\":\"", "");
                    matchText = matchText.substring(0, matchText.length() - 2);

                    spuId = matchText;

                }
            }

            //主图视频url
            if ("C".equals(userType)) {//集市
                start = Pattern.compile("\"videoId\":\"\\d+?\",");
                ms = start.matcher(resBuffer.toString());
                if (ms.find()) {
                    String videoId = "";
                    String userId = "";
                    String matchText = ms.group();
                    matchText = matchText.replace("\"videoId\":\"", "");
                    matchText = matchText.substring(0, matchText.length() - 2);
                    if (StringUtil.isNotEmpty(matchText)) {
                        videoId = matchText.trim();
                        start = Pattern.compile("userid=\\d+?;");
                        ms = start.matcher(resBuffer.toString());
                        if (ms.find()) {
                            userId = ms.group();
                            userId = userId.replace("userid=", "").replace(";", "");
                        }
                    }

                    //cloud.video.taobao.com/play/u/1746960915/p/2/e/6/t/1/50166818110.mp4
                    masterVideoUrl = "//cloud.video.taobao.com/play/u/" + userId + "/p/2/e/6/t/1/" + videoId + ".mp4";

                } else {
                    start = Pattern.compile("valFlashUrl:\\s*?\".*?\"");
                    ms = start.matcher(resBuffer.toString());
                    if (ms.find()) {
                        String matchText = ms.group();
                        matchText = matchText.replaceAll("valFlashUrl:\\s*?\"", "");
                        matchText = matchText.substring(0, matchText.length() - 1);
                        if (StringUtil.isNotEmpty(matchText)) {
                            masterVideoUrl = matchText.trim();
                        }
                    }
                }
            } else {//天猫
                start = Pattern.compile("\"imgVedioUrl\":\"((?!,).)*?\",");
                ms = start.matcher(resBuffer.toString());
                if (ms.find()) {
                    String matchText = ms.group();
                    matchText = matchText.replace("\"imgVedioUrl\":\"", "").replace("\",", "");
                    if (StringUtil.isNotEmpty(matchText)) {
                        masterVideoUrl = matchText.trim();
                    }
                }
            }

            //资质图
            try {
                start = Pattern.compile("g_config\\.itemQualification = \\{.*?\\};");//bug30893
                ms = start.matcher(resBuffer.toString());
                if (ms.find()) {
                    String matchText = ms.group();
                    matchText = matchText.replace("g_config.itemQualification = ", "");
                    Map<String, Object> itemQualification = Constant.OBJECTMAPPER.readValue(matchText, new TypeReference<Map<String, Object>>() {
                    });
                    if (itemQualification != null) {
                        List<String> imageList = (List<String>) itemQualification.get("imageList");
                        if (ValidateUtil.isNotEmpty(imageList)) {
                            if (StringUtil.isNotEmpty(imageList.get(0))) {
                                if (matchText.contains("能效标签")) {
                                    qualiImgUrl = "energy:" + imageList.get(0);
                                } else if (matchText.contains("医疗器械注册证")) {
                                    qualiImgUrl = "medical:" + imageList.get(0);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {

            }

            try {
                start = Pattern.compile("商品具有：<a href=\"//baike.taobao.com/list.htm\\?q=.*?\".*?>食品添加剂生产许可证编号</a>");
                ms = start.matcher(resBuffer.toString());
                if (ms.find()) {
                    String matchText = ms.group();
                    matchText = matchText.replace("商品具有：<a href=\"//baike.taobao.com/list.htm?q=", "")
                            .replaceAll("\".*?>食品添加剂生产许可证编号</a>", "");
                    if (StringUtil.isNotEmpty(matchText)) {
                        foodAddCer = matchText;
                    }
                }
            } catch (Exception e) {

            }
            Map<String, String> crawlMap = new HashMap<String, String>();
            crawlMap.put("isExists", isExists);

            crawlMap.put("title", title);
            crawlMap.put("userType", userType);
            crawlMap.put("nick", nick);
            crawlMap.put("shopName", shopName);
            crawlMap.put("cid", cid);
            crawlMap.put("picUrl", picUrl);

            crawlMap.put("title", title);
            crawlMap.put("desc", descUrl);
            crawlMap.put("skuMap", skuMapStr);
            crawlMap.put("subTitle", subTitle);
            crawlMap.put("cccNum", cccNum);
            crawlMap.put("isSecond", isSecond);
            crawlMap.put("isGlobal", isGlobal);
            crawlMap.put("isCustomerMade", isCustomerMade);
            if (StringUtil.isNotEmpty(itemImgStr)) {
                crawlMap.put("itemImgStr", itemImgStr);
            }
            if (StringUtil.isNotEmpty(induCerCode)) {
                crawlMap.put("induCerCode", induCerCode);
            }
            if (StringUtil.isNotEmpty(price)) {
                crawlMap.put("price", price);
            }
            if (StringUtil.isNotEmpty(userType)) {
                crawlMap.put("userType", userType);
            }
            if (StringUtil.isNotEmpty(location)) {
                crawlMap.put("location", location);
            }
            if (StringUtil.isNotEmpty(cid)) {
                crawlMap.put("cid", cid);
            }
            if (StringUtil.isNotEmpty(sellerCids)) {
                crawlMap.put("sellerCids", sellerCids);
            }
            if (StringUtil.isNotEmpty(spuId)) {
                crawlMap.put("spuId", spuId);
            }
            if (StringUtil.isNotEmpty(masterVideoUrl)) {
                crawlMap.put("masterVideoUrl", masterVideoUrl);
            }
            if (StringUtil.isNotEmpty(qualiImgUrl)) {
                crawlMap.put("qualiImgUrl", qualiImgUrl);
            }
            if (StringUtil.isNotEmpty(foodAddCer)) {
                crawlMap.put("foodAddCer", foodAddCer);
            }
            crawlMap.put("skuWebData", skuWebDataStr);
            crawlMap.put("catPropList", catPropList);
            return crawlMap;

        }
    }
}
