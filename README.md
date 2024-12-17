# Jvppeteer
<p style="text-align:left">
 <a><img alt="Maven Central" src="https://img.shields.io/maven-central/v/io.github.fanyong920/jvppeteer"></a>
    <a href="https://sonarcloud.io/dashboard?id=fanyong920_jvppeteer"><img alt="Quality Gate Status" src="https://sonarcloud.io/api/project_badges/measure?project=fanyong920_jvppeteer&metric=alert_status" style="max-width:100%;"></a>
</p>

# Java API For Chrome and Firefox


Jvppeteer 通过 [DevTools](https://chromedevtools.github.io/devtools-protocol/)  and [WebDriver](https://pptr.nodejs.cn/webdriver-bidi) 控制 Chrome for Testing（下面简称 Chrome）或 Firefox。目前，Jvppeteer 仅支持通过 [DevTools](https://chromedevtools.github.io/devtools-protocol/) 控制 Chrome、默认通过 [WebDriver](https://pptr.nodejs.cn/webdriver-bidi) 控制 Firefox。 默认情况下，以  headless （无 UI）模式运行，也可以通过配置运行'有UI'模式。


你可以在浏览器中手动执行的绝大多数操作都可以使用 Jvppeteer 来完成！ 下面是一些示例：

- 生成页面 PDF。
- 抓取 SPA（单页应用）并生成预渲染内容（即“SSR”（服务器端渲染））。
- 自动提交表单，进行 UI 测试，键盘输入等。
- 创建一个时时更新的自动化测试环境。 使用最新的 JavaScript 和浏览器功能直接在最新版本的 Chrome 中执行测试。
- 捕获网站的 [timeline trace](https://developers.google.com/web/tools/chrome-devtools/evaluate-performance/reference)，用来帮助分析性能问题。
- 测试浏览器扩展。

## 开始使用

### 一、以下是使用依赖管理工具（如 maven 或 gradle）的简要指南。

#### Maven
要使用 maven,请将此依赖添加到pom.xml文件中：

```xml
<dependency>
  <groupId>io.github.fanyong920</groupId>
  <artifactId>jvppeteer</artifactId>
  <version>2.2.5<version>
</dependency>
```

#### Gradle

要使用 Gradle，请将 Maven 中央存储库添加到您的存储库列表中:

```
mavenCentral（）
```

然后，您可以将最新版本添加到您的构建中。

```xml
compile "io.github.fanyong920:jvppeteer:2.2.5"
```

#### Logging

该库使用 [SLF4J](https://www.slf4j.org/) 进行日志记录，并且不附带任何默认日志记录实现。

调试程序将日志级别设置为 Debug。

#### 独立 jar

如果您不使用任何依赖项管理工具，则可以在[此处](https://github.com/fanyong920/jvppeteer/releases/latest)找到最新的独立 jar 。

### 二、快速开始

#### 1、下载浏览器

从2.0.0版本开始，Jvppeteer与 [Chrome ](https://googlechromelabs.github.io/chrome-for-testing/#stable) 配合使用， [Chrome ](https://googlechromelabs.github.io/chrome-for-testing/#stable) 使用的是新的无头模式，旧的无头模式现在是一个名为 [chrome-headless-shell](https://developer.chrome.com/blog/chrome-headless-shell) 的独立程序。无论你使用 [Chrome](https://googlechromelabs.github.io/chrome-for-testing/#stable) 还是 [chrome-headless-shell](https://developer.chrome.com/blog/chrome-headless-shell) ，你只要将 headless 设置为 true， Jvppeteer 会自动匹配新的无头模式和旧的无头模式.

在此版本之前，Jvppeteer 下载并与 Chromium 配合使用。

下面是使用 Jvppeteer 下载浏览器的一些例子：

```java
    //采用默认配置下载浏览器，默认采用Chrome for Testing 浏览器。版本号Constant#Version
    RevisionInfo revisionInfo = Puppeteer.downloadBrowser();
    System.out.println("revisionInfo: " + revisionInfo);

    //下载指定版本的chrome for Testing浏览器
    RevisionInfo revisionInfo2 = Puppeteer.downloadBrowser("128.0.6613.137");
    System.out.println("revisionInfo2: " + revisionInfo2);

    //下载指定版本的ChromeDriver
    FetcherOptions fetcherOptions = new FetcherOptions();
    fetcherOptions.setProduct(Product.CHROMEDRIVER);
    fetcherOptions.setVersion("129.0.6668.100");
    RevisionInfo revisionInfo3 = Puppeteer.downloadBrowser(fetcherOptions);
    System.out.println("revisionInfo3: " + revisionInfo3);

    //下载指定版本的Chrome Headless Shell
    FetcherOptions fetcherOptions2 = new FetcherOptions();
    fetcherOptions2.setProduct(Product.CHROMEHEADLESSSHELL);
    fetcherOptions2.setVersion("129.0.6668.100");
    RevisionInfo revisionInfo4 = Puppeteer.downloadBrowser(fetcherOptions2);
    System.out.println("revisionInfo4: " + revisionInfo4);

    //下载指定版本的CHROMIUM
    FetcherOptions fetcherOptions3 = new FetcherOptions();
    fetcherOptions3.setProduct(Product.CHROMIUM);
    fetcherOptions3.setVersion("1366415");
    RevisionInfo revisionInfo5 = Puppeteer.downloadBrowser(fetcherOptions3);
    System.out.println("revisionInfo5: " + revisionInfo5);
```

Jvpeteer 提供 Chrome、Chromium、ChromeDriver、Chrome Headless Shell 四种浏览器的下载功能。

下载 Chromium、ChromeDriver、Chrome Headless Shell 必须明确下载版本，Chrome 有默认版本，存放在BrowserRevision.class 中。

关于下载浏览器的版本选择，可以浏览一下这两个网页：[Chrome for Testing availability](https://googlechromelabs.github.io/chrome-for-testing/#stable) 与  [JSON API endpoints](https://github.com/GoogleChromeLabs/chrome-for-testing)

**Mac必须withExcutablePath是用來指定启动Chrome.exe的路径。在Mac下载浏览器有问题。**

#### 2、支持的浏览器版本列表

每个 Jvppeteer 版本都有一个绑定的浏览器版本，最好采用绑定的浏览器版本使用 Jvppeteer。

下表提供了 Jvppeteer 版本与绑定的浏览器版本之间的映射。如果没有列出完全匹配的 Jvppeteer 版本，则支持的浏览器版本是紧接在前的版本：

|    3.0.0     | [Chrome for Testing](https://googlechromelabs.github.io/chrome-for-testing/#stable) 131.0.6778.87  & Firefox stable_133.0 |
| :----------: | :----------------------------------------------------------: |
| 2.2.0--2.2.5 | [Chrome for Testing](https://googlechromelabs.github.io/chrome-for-testing/#stable) 130.0.6723.58 |
|    2.1.2     | [Chrome for Testing](https://googlechromelabs.github.io/chrome-for-testing/#stable) 128.0.6613.137 |
|    2.1.1     | [Chrome for Testing](https://googlechromelabs.github.io/chrome-for-testing/#stable) 128.0.6613.137 |
|    2.1.0     | [Chrome for Testing](https://googlechromelabs.github.io/chrome-for-testing/#stable) 128.0.6613.137 |
|    2.0.0     | [Chrome for Testing](https://googlechromelabs.github.io/chrome-for-testing/#stable) 128.0.6613.137 |
|    1.1.6     |                       Chromium 722234                        |
|    1.1.5     |                       Chromium 722234                        |
|    1.1.4     |                       Chromium 722234                        |
|    1.1.3     |                       Chromium 722234                        |
|    1.1.2     |                       Chromium 722234                        |

#### 3、启动浏览器

下载浏览器之后，我们就可以使用它与 Jvppeter 一起工作了，首先启动浏览器。

```java
	Puppeteer.launch();
	//或者
	Puppeteer.launch(launchOptions);
```

可以采用默认的配置启动浏览器，默认是无界面模式的，也可以添加可选配置 options 启动浏览器。

#### 3、关闭浏览器

使用浏览器后，必须关闭它，使用 Browser.close() 关闭。

```java
 	Browser cdpBrowser = Puppeteer.launch();	
 	Page page = cdpBrowser.newPage();
	cdpBrowser.close();
```

#### 4、浏览器上下文

如果你需要隔离自动化任务，请使用 BrowserContexts。Cookie 和本地存储不在浏览器上下文之间共享。

```java
    Browser cdpBrowser = Puppeteer.launch(launchOptions);
    BrowserContext defaultCdpBrowserContext = cdpBrowser.defaultCdpBrowserContext();
    Page page = defaultCdpBrowserContext.newPage();
    new Thread(() -> {
        try {
            page.evaluate("() => window.open('https://www.example.com/')");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }).start();
    Target target1 = defaultCdpBrowserContext.waitForTarget(target -> target.url().equals("https://www.example.com/"));
    System.out.println("target1:" + target1.url());
    List<Page> pages = defaultCdpBrowserContext.pages();
    System.out.println("size1:" + pages.size());
    defaultCdpBrowserContext.newPage();
    System.out.println("size2:" + defaultCdpBrowserContext.pages().size());
    List<Target> targets = defaultCdpBrowserContext.targets();
    for (Target target : targets) {
        System.out.println("all target forEach:(" + target.type() + ":" + target.url() + ")");
    }
    BrowserContext cdpBrowserContext = cdpBrowser.createBrowserContext();
    Page page1 = cdpBrowserContext.newPage();
    Browser cdpBrowser1 = cdpBrowserContext.cdpBrowser();
    System.out.println("broswer equals:" + (cdpBrowser1 == cdpBrowser));
    cdpBrowserContext.overridePermissions("https://www.baidu.com", WebPermission.GEOLOCATION);
    page1.goTo("https://www.baidu.com");
    cdpBrowserContext.close();
    System.out.println("close: " + cdpBrowserContext.closed());
    //默认浏览器不能关闭
    defaultCdpBrowserContext.close();
    Thread.sleep(15000);
    cdpBrowser.close();
```

在浏览器上下文中，你可以打开一个新的页面，可以获得浏览器上下文的所有页面，可以通过关闭 浏览器上下文 来关闭 对应的所有页面，也可以给浏览器上下文授予独特的权限。**提醒 ：创建多个浏览器上下文比创建多个浏览器好 **

#### 5、连接到远程的浏览器

如果你在 Jvppeteer 之外打开了一个新的浏览器，你可以用 Puppeteer.connect() 方法连接，连接远程的浏览器需要 URL，该 URL 可以是 Websocket URL（格式是 ws://HOST:PORT/devtools/cdpBrowser/<id>），也可以是 Browser URL （格式是 http://HOST:PORT ）。

在 Browser URL  中的 PORT 是 debuggingPort，在浏览器启动时候加上参数：-remote-debugging-port=xxxx，debuggingPort 即 xxxx。

Browser URL 后加上 /json/version，格式是：http://HOST:PORT/json/version 可以获取到 WebSocket URL

```java
    String wsEndpoint = cdpBrowser.wsEndpoint();
    cdpBrowser.disconnect();
    //ws连接
    Browser wsCdpBrowser = Puppeteer.connect(wsEndpoint);
    wsCdpBrowser.disconnect();
    //url连接 http://host:port  因为启动时候配置DebuggingPort=9222  所以url = localhost:9222
    Browser urlCdpBrowser = Puppeteer.connect("http://localhost:9222");
```

#### 6、页面打印PDF

要打印 PDF，请使用 Page.pdf() 。手动在浏览器按Ctrl+P可以预览 PDF，弹出的窗口就是 PDF 样式，把各个选项点一下，看一下预览效果 然后再回来写代码。

```java
    //pdf必须配置headless = true
    launchOptions.setHeadless(true);
    ArrayList<String> args = new ArrayList<>();//添加一些额外的启动参数
    args.add("--no-sandbox");//pdf必须添加这个参数,不然无法打印，具体看这里https://github.com/puppeteer/puppeteer/issues/12470
    launchOptions.setArgs(args);
    Browser cdpBrowser = Puppeteer.launch(launchOptions);
    Page page = cdpBrowser.newPage();
    GoToOptions goToOptions = new GoToOptions();
    goToOptions.setWaitUntil(Collections.singletonList(PuppeteerLifeCycle.NETWORKIDLE));
    page.goTo("https://www.baidu.com/?tn=68018901_16_pg",goToOptions);
    PDFOptions pdfOptions = new PDFOptions();
    pdfOptions.setPath("baidu.pdf");
    pdfOptions.setOutline(true);//生成大纲
    pdfOptions.setFormat(PaperFormats.a4);//A4大小
    pdfOptions.setPrintBackground(true);//打印背景图形，百度一下这个蓝色按钮就显示出来了
    pdfOptions.setPreferCSSPageSize(false);
    pdfOptions.setScale(1.1);//缩放比例1.1
    page.pdf(pdfOptions);
    //关闭浏览器
    cdpBrowser.close();
```

默认情况下，Page.pdf() 等待字体加载。

#### 7、页面截图

要捕获屏幕截图，请使用 Page.screenshot()。

```java
	@Test
    public void test3() throws Exception {
        Browser cdpBrowser = Puppeteer.launch(launchOptions);
        Page page = cdpBrowser.newPage();
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        screenshotOptions.setPath("baidu.png");
        screenshotOptions.setOmitBackground(true);
        //全屏截图
        screenshotOptions.setFullPage(true);
        //截图的更多
        screenshotOptions.setCaptureBeyondViewport(true);
        page.screenshot(screenshotOptions);
        cdpBrowser.close();
    }

    @Test
    public void test4() throws Exception {
        Browser cdpBrowser = Puppeteer.launch(launchOptions);
        Page page = cdpBrowser.newPage();
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        screenshotOptions.setPath("baidu.png");
        //指定图片类型，path指定的名称中的后缀便不起作用了
        screenshotOptions.setType(ImageType.JPEG);
        //jpg可以设置这个选项
        screenshotOptions.setQuality(80.00);
        //全屏截图
        screenshotOptions.setFullPage(true);
        page.screenshot(screenshotOptions);
        cdpBrowser.close();
    }

    @Test
    public void test5() throws Exception {
        Browser cdpBrowser = Puppeteer.launch(launchOptions);
        Page page = cdpBrowser.newPage();
        page.goTo("https://www.baidu.com/?tn=68018901_16_pg");
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        screenshotOptions.setPath("baidu.jpeg");
        //指定图片类型，path指定的名称中的后缀便不起作用了
        screenshotOptions.setType(ImageType.WEBP);
        //jpg可以设置这个选项
        screenshotOptions.setQuality(80.00);
        //全屏截图
        screenshotOptions.setFullPage(true);
        page.screenshot(screenshotOptions);
        cdpBrowser.close();
    }
```

#### 8、录制屏幕

要捕录制屏幕，请使用 Page.screencast()。

```java
	/**
     * 录制屏幕某个区域 录制格式webm
     */
    @Test
    public void test25() throws IOException {
        Browser cdpBrowser = Puppeteer.launch(launchOptions);
        Page page = cdpBrowser.newPage();
        page.goTo("https://www.geetest.com/demo/slide-en.html");
        ScreencastOptions screencastOptions = new ScreencastOptions();
        screencastOptions.setPath("D:\\test\\test2.webm");
        screencastOptions.setFormat(ScreenCastFormat.WEBM);
        //指定ffmpeg路径，如果配置了系统的环境变量，那么可以不指定
        BoundingBox boundingBox = page.$("#username").boundingBox();
        screencastOptions.setCrop(boundingBox);
        screencastOptions.setFfmpegPath("D:\\windowsUtil\\ffmpeg.exe");
        ScreenRecorder screencast = page.screencast(screencastOptions);
        page.type("#username", "123456789", 200);
        page.type("#password", "123456789", 200);
        screencast.stop();
        cdpBrowser.close();
    }

	/**
     * 录制屏幕 录制格式gif
     */
    @Test
    public void test26() throws IOException {
        Browser cdpBrowser = Puppeteer.launch(launchOptions);
        Page page = cdpBrowser.newPage();
        page.goTo("https://www.geetest.com/demo/slide-en.html");
        ScreencastOptions screencastOptions = new ScreencastOptions();
        screencastOptions.setPath("D:\\test\\test.gif");
        screencastOptions.setFormat(ScreenCastFormat.GIF);
        screencastOptions.setFfmpegPath("D:\\windowsUtil\\ffmpeg.exe");
        ScreenRecorder screencast = page.screencast(screencastOptions);
        page.type("#username", "123456789", 200);
        page.type("#password", "123456789", 200);
        screencast.stop();
        cdpBrowser.close();
    }
```

#### **更多的例子请看项目内的example文件夹** 点击[这里](https://github.com/fanyong920/jvppeteer/tree/master/example/src/main/java/com/ruiyun/example)，是入门级别的例子，对如何使用 Jvppeteer 有很大帮助。

### 三、遇到问题怎么办

如果你在 Linux 上安装 Chrome 并运行 遇到麻烦，或者在某个场景中遇到麻烦，可以 在 [Puppeteer(Node.js)](https://github.com/puppeteer/puppeteer) 库中的 [Troubleshooting](https://pptr.dev/troubleshooting) 寻找答案，也可以在其 issues 中寻找一些解决问题的思路，或者google baidu puppeteer的解决方案，再应用到你的问题上。

### 四、资源

1. [Puppeteer中文文档](https://pptr.nodejs.cn/) : 更加详细的 API 文档 ，多看看了解一下
2. [DevTools Protocol](https://chromedevtools.github.io/devtools-protocol/) ：CDP 协议
3. [本项目的WIKI]([Home · fanyong920/jvppeteer Wiki](https://github.com/fanyong920/jvppeteer/wiki)) 目前写有一些内容，后续有想到新的，会持续更新
4. [命令行启动参数大全](https://github.com/GoogleChrome/chrome-launcher/blob/d6be1f3250ef7ff7648ae58c4e92e48509bdbe7c/src/flags.ts#L67)

### 五、执照

此仓库中找到的所有内容均已获得 Apache 许可。有关详细信息，请参见`LICENSE`文件
