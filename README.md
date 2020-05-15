# Jvppeteer
<p align = "left">
<a rel="nofollow" href="https://download-chromium.appspot.com/"><img src ="https://img.shields.io/badge/chromium%20download-latest-blue"  alt="下载最新版本的chromuim" style="max-width:100%;"></a> <a><img alt="maven仓库" src="https://img.shields.io/maven-central/v/com.ruiyun/jvppeteer/1.0.0" style="max-width:100%;"></a> <a href="https://github.com/fanyong920/jvppeteer/issues"><img alt="Issue resolution status" src="https://img.shields.io/github/issues/fanyong920/jvppeteer" style="max-width:100%;"></a>
</p>



**本库的灵感来自[Puppeteer(Node.js)](https://github.com/puppeteer/puppeteer),API也与其基本上保持一致，做这个库是为了方便使用Java操控Chrome 或 Chromium**




   >Jvppeteer通过[DevTools](https://chromedevtools.github.io/devtools-protocol/)控制 Chromium 或 Chrome。
   >默认情况下，以headless模式运行，也可以通过配置运行'有头'模式。


你可以在浏览器中手动执行的绝大多数操作都可以使用 Jvppeteer 来完成！ 下面是一些示例：

- 生成页面 PDF。J
- 抓取 SPA（单页应用）并生成预渲J染内容（即“SSR”（服务器端渲染））。
- 自动提交表单，进行 UI 测试，键盘输入等。
- 创建一个时时更新的自动化测试环境。 使用最新的 JavaScript 和浏览器功能直接在最新版本的Chrome中执行测试。
- 捕获网站的 [timeline trace](https://developers.google.com/web/tools/chrome-devtools/evaluate-performance/reference)，用来帮助分析性能问题。
- 测试浏览器扩展。

## 开始使用

### 以下是使用依赖管理工具（如maven或gradle）的简要指南。
#### Maven
要使用maven,请将此依赖添加到pom.xml文件中：

```xml
<dependency>
  <groupId>com.ruiyun</groupId>
  <artifactId>jvppeteer</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
 <!--我现在还没发布到maven，只是提前先写出来 -->
```

#### Gradle

要使用Gradle，请将Maven中央存储库添加到您的存储库列表中:

```
mavenCentral（）
```

然后，您可以将最新版本添加到您的构建中。

```xml
compile "com.ruiyun:jvppeteer:1.0.0-SNAPSHOT"
```

#### Logging

该库使用[SLF4J](https://www.slf4j.org/)进行日志记录，并且不附带任何默认日志记录实现。

调试日志将使用日志级别进行DEBUG。

### 接下来将给出几个例子：

#### 启动：

```java
//设置基本的启动配置,这里选择了‘有头’模式启动
LaunchOptions options = new OptionsBuilder().withHeadless(false).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();
//启动
Puppeteer.launch(options);
```

在这个例子中，我们明确指明了启动路径，程序就会根据指明的路径启动对应的浏览器，如果没有明确指明路径，那么程序会尝试启动默认安装路径下的Chrome浏览器

#### 导航：

```java
ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(false).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://www.taobao.com/about/");
        browser.close();
```

这个例子中，浏览器导航到具体某个页面后关闭。在这里并没有指明启动路径。arrayList是放一些额外的命令行启动参数的，在下面资源章节中我会给出相关资料。

#### 生成PDF：

```java
ArrayList<String> arrayList = new ArrayList<>();
String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe;
//生成pdf必须在无厘头模式下才能生效
LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(true).withExecutablePath(path).build();
arrayList.add("--no-sandbox");
arrayList.add("--disable-setuid-sandbox");
Browser browser = Puppeteer.launch(options);
Page page = browser.newPage();
page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
PDFOptions pdfOptions = new PDFOptions();
pdfOptions.setPath("test.pdf");
page.pdf(pdfOptions);
```

在这个例子中，导航到某个页面后，将整个页面截图，并写成PDF文件。注意，生成PDF必须在headless模式下才能生效

#### TRACING:性能分析

```java
ArrayList<String> arrayList = new ArrayList<>();
String path = "D:\\develop\\project\\toString\\chrome-win\\chrome.exe";

LaunchOptions options = new OptionsBuilder().withArgs(arrayList).withHeadless(true).withExecutablePath(path).build();
arrayList.add("--no-sandbox");
arrayList.add("--disable-setuid-sandbox");
Browser browser = Puppeteer.launch(options);

Page page = browser.newPage();
//开启追踪
page.tracing().start("C:\\Users\\howay\\Desktop\\trace.json",true,new HashSet<>());
page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
//发出追踪结束信号
page.tracing().stop();
```

在这个例子中，将在页面导航完成后，生成一个json格式的文件，里面包含页面性能的具体数据，可以用Chrome浏览器开发者工具打开该json文件，并分析性能。

**更多的例子请看**[这里](https://github.com/fanyong920/jvppeteer/tree/master/src/test/java/com/ruiyun/test)

### 资源

1. [Puppeteer中文文档](https://zhaoqize.github.io/puppeteer-api-zh_CN/#/)
2. [DevTools Protocol](https://chromedevtools.github.io/devtools-protocol/)
3. [Chrome命令行启动参数](https://peter.sh/experiments/chromium-command-line-switches/)

### 执照

此仓库中找到的所有内容均已获得Apache许可。有关详细信息，请参见`LICENSE`文件