# Change log

## Version Release 1.0.2 (2020/07/18)

#### Bugs Fixed

- 2020-07-03解决Response的buffer为null的问题
- 2020-07-12解决Page#setCookie失效问题
- 2020-07-18解决Page#reload过程中可能出现的超时现象
- 2020-07-18解决Page#newPage失败问题

## Version Release 1.0.3 (2020/07/24)

#### Bugs Fixed
- 2020-07-21解决ElementHandle#getBoxModel返回null问题
- 2020-07-23解决页面关闭后log打印异常信息问题
- 2020-07-24解決newPage出現的超时問題

## Version Release 1.0.4 (2020/07/30)

#### Bugs Fixed

- [修改视口的宽高为in类型；更改删除缓存文件夹的方式；对pdf方法添加判断空指针问题](https://github.com/fanyong920/jvppeteer/commit/966ce2f59b5c72297c7840b4c5fa9426a3b76b95)
- [去掉jdk扩展包中类的引用](https://github.com/fanyong920/jvppeteer/commit/36a52100f26ad88e33e2c2a7cfdd0d1ecc8529c8)

#### New Feature

- [截图时设置色盲模式](https://github.com/fanyong920/jvppeteer/commit/952da3c4065a948b7a13db838496059adaafc127)

- [添加自动下载chrome功能](https://github.com/fanyong920/jvppeteer/commit/6414a980e85789c1c8abc273ada5b484345840ac)

## Version Release 1.0.5 (2020/08/11)

#### Bugs Fixed

- [修正下载回调中出现的除法报错](https://github.com/fanyong920/jvppeteer/commit/3ea31516b77cb614711b957be19746da297f390c)
- [设置Page#setUserAgent方法为public](https://github.com/fanyong920/jvppeteer/commit/d0f996ca921bd90273fd1165337371bc0bf87350)

#### New Feature

- [Page#pdf接口返回byte[\]](https://github.com/fanyong920/jvppeteer/commit/7f4f29949b736daa05e958df4ad6a10a0cd6539b)

## Version Release 1.0.6 (2020/08/15)

#### Bugs Fixed

- [修复鼠标移动无效的问题](https://github.com/fanyong920/jvppeteer/commit/96801463efcbb912862151a0615bfe760e64f931)
- [修复Response body is unavailable for redirect responses报错](https://github.com/fanyong920/jvppeteer/commit/bd630850b1f3ccc4e0aeac51755cff8b94dd2ea0)
- [尝试修复解压dmg文件出错的问题](https://github.com/fanyong920/jvppeteer/commit/379910ed478b0471b0bdda3211c24dbeba48e6ce)

#### New Feature

[添加华为手机设备；添加鼠标滚轮功能](https://github.com/fanyong920/jvppeteer/commit/acc859f9d0163dc600a104d74e63f511a61fc038)

## Version Release 1.0.7 (2020/08/28)

#### Bugs Fixed

- [修复ElementHandle类型转换错误的问题](https://github.com/fanyong920/jvppeteer/commit/13b7dca31eb50a52ebdf52a32ec8ec27d6e20d1b)
- [修复Page#evaluateOnNewDocument失效问题](https://github.com/fanyong920/jvppeteer/commit/9dce424656d1f2042c12d1b4be8ce2e5af9dfca7)
- [超时时间判断失效问题](https://github.com/fanyong920/jvppeteer/commit/8c278bc3e914ca15dcff084c55245c69598f3c73)

#### New Feature

- [简化Page#evaluate等方法](https://github.com/fanyong920/jvppeteer/commit/98e30907ecee0cda9ceeadebe5aa4a10d07bc881)
- [Connection等待时长为无限](https://github.com/fanyong920/jvppeteer/commit/b6695b35d6c8652d0800340d1fcc402c4ae3bf71)

## Version Release 1.0.8 (2020/09/01)

#### Bugs Fixed

- [修改可能出现的无限等待问题](https://github.com/fanyong920/jvppeteer/commit/901fa82db5be2e1df9c85a56e3df013118f7a0d0)

## Version Release 1.0.9 (2020/09/18)

#### Bugs Fixed

- [尝试修改卡死的问题](https://github.com/fanyong920/jvppeteer/commit/be2b07a2a0242735e04e23ff18bb952bfa8bbda2)

- [给监听事件给上异步操作](https://github.com/fanyong920/jvppeteer/commit/0ea7abe7e96efb6ac453d5c0e8723542dd692c8a)

- [修复Page#waitForXpath方法出错](https://github.com/fanyong920/jvppeteer/commit/31c6e3aa5768fafb87a3764e7634fa783d34a30c)
- [去掉方法签名上不必要的异常](https://github.com/fanyong920/jvppeteer/commit/1ff74c8fbbb21f2d63933a98ae73d9d229f2dc34)

#### New Feature

- [暴露on方法实现自定义事件监听](https://github.com/fanyong920/jvppeteer/commit/817a1ab012eea5785dfe6d2c5dd8e2fa0aabd4fe)

## Version Release 1.1.0 (2020/09/29)

#### Bugs Fixed

- [统一优先采用Process类关闭浏览器](https://github.com/fanyong920/jvppeteer/commit/f9f2a3f3d8e4740e1290dd41300080654cb54db2)
- [浏览器关闭时，对正在等待的callback进行释放，防止卡死](https://github.com/fanyong920/jvppeteer/commit/c2af123627032380c072bc6a17fadd7864355fa4)
- [刷新頁面採用新的單獨線程池](https://github.com/fanyong920/jvppeteer/commit/b17c630d247f8dfb457f56d7241c96f45f183bac)

### Version Release 1.1.1 (2020/11/20)

#### Bugs Fixed

- [2020-10-13 修复Page#select中执行js脚本时参数不对的问题](https://github.com/fanyong920/jvppeteer/commit/c2f2de68fbbd212d942c818254bdb927011376ae)
- [2020-10-19 修复Request#continueRequest的问题](https://github.com/fanyong920/jvppeteer/commit/6684650c44c7daa027ca17529b2eb26f36730a50)
- [2020-10-28 修复Coverage超时的问题](https://github.com/fanyong920/jvppeteer/commit/e0ceeeb33ffc0c7899b3161257f4ba0efc461e1d)
- [2020-11-19 修复Page#goTo方法中extraHttpHeader不起作用的问题](https://github.com/fanyong920/jvppeteer/commit/d1545e2b1e1f7bc09e9be38334cca771c1b57309)
- [2020-11-20 修复Page#close方法花费30s的问题](修复一些bug，具体见[CHANGELOG](https://github.com/fanyong920/jvppeteer/blob/master/CHANGELOG.md))

### Version Release 1.1.2 (2021/02/15)

#### Bugs Fixed

- [2021-01-27 修复Page#goTo方法中referrer值为null的问题](https://github.com/fanyong920/jvppeteer/commit/89135bb8c604db09735caf63f5a575da6288ef7c)
- [修复截图前可能重新加载导致找不到context](https://github.com/fanyong920/jvppeteer/commit/b94d3b927f2bb73871540059c7f7520685465b62)

### Version Release 1.1.3 (2021/04/04)

- [修正Page#$x()方法中存在的问题](https://github.com/fanyong920/jvppeteer/commit/a7fe287f900c61e09d621bc29394af6df250e790)
- [FIX:修复启动器不能通过LaunchOptions的setProduct方法切换FIREFOX的问题](https://github.com/fanyong920/jvppeteer/commit/d4eb28fc986aa92357a515bd2fa93323501a6546)
- [2021-03-06 修正DefaultBrowserListener中变量名mothod为method](https://github.com/fanyong920/jvppeteer/commit/a2247ec0d3272bc0f2da823ce620bb36d447f102)
- [INF:修改Page类以及引用的Example中onDialg方法名拼写错误; 增加Page扩展类PageExtend](https://github.com/fanyong920/jvppeteer/commit/60c8f74a5f04de1f90bd193a655e965dabc35b22)
- [INF: Page扩展类修改，增加获取页面文本](https://github.com/fanyong920/jvppeteer/commit/65a5427d5a57bbf09632e9df46ceb76a56a55e14)

### Version Release 1.1.4 (2021/10/30)

- [2021-10-23 解压Chrome.zip压缩包时关闭流，防止出现text file busy错误](https://github.com/fanyong920/jvppeteer/commit/6cd7a06528eb222d8c7947ea632c1a1761b40a6b)

### Version Release 1.1.5 (2022/4/30)

- [ElementHandle添加带有scrollIntoViewIfNeeded参数的screenshot方法](https://github.com/fanyong920/jvppeteer/commit/f981f4b867c9baf2ea0537c9088b39f728d1e059)

- [#88](https://github.com/fanyong920/jvppeteer/issues/88) [jackson-databind版本升级至2.10.5.1](https://github.com/fanyong920/jvppeteer/commit/01ce3fe74e3fdee63ac6922e0b02969ba9058fca)

- [#91](https://github.com/fanyong920/jvppeteer/issues/91) [fix respond新增重载方法，指定是否需要base64解码body数据，同时兼容mime标准base64。原方法默认解码](https://github.com/fanyong920/jvppeteer/commit/775a6c471ac80958788f21307a216c420052dfe4)

- [Bump zip4j from 2.7.0 to 2.9.1](https://github.com/fanyong920/jvppeteer/commit/5e0bbeb5651d67c0fdd2d1f4c187d24a8ed1dfcc)

### Version Release 1.1.6(2024/4/30)

- [过kill杀死linux上残留的chrome进程](https://github.com/fanyong920/jvppeteer/commit/efc9a2404e922b8dbbedba82b33bc11b005e0284)
- [Puppeteer增加指定版本启动浏览器](https://github.com/fanyong920/jvppeteer/commit/6b8855c5f01ddd64dfbc5a0a1b98f8733afed02a)

### Version Release 2.0.0(2024/10/15)

- 重写

### Version Release 2.1.0(2024/10/22)

- [修复 Frame.java 的 frameElement 方法](https://github.com/fanyong920/jvppeteer/commit/25106d311a68f00495d0576f29a27042236d442f)
- [添加 Page#screencast 方法用于屏幕录制](https://github.com/fanyong920/jvppeteer/commit/eb6f814a83f0a8c3c3225775c4635bf800d997ca)
- [有小数点的 时间戳用 BigDecimal 表示](https://github.com/fanyong920/jvppeteer/commit/d949ac933ebd1666f32d9cface4624935709a280)
- [修复：在拦截期间处理 RequestServedFromCache](https://github.com/fanyong920/jvppeteer/commit/8926db2952ab718132d7ecc32f5b7c9a8e451878)
- [将 Puppeteer 的默认参数和启动路径方法移动到 Browser](https://github.com/fanyong920/jvppeteer/commit/b62600cab188c994886bb0a730e9a752d3a0f593)
- [将 ScreenRecorder 的删除临时照片由系统退出时删除改为录制结束时删除](https://github.com/fanyong920/jvppeteer/commit/54448f2a1011001fdf33112e16346ac3a96c0ded)
- [AwaitableResult 的结果可见性修改](https://github.com/fanyong920/jvppeteer/commit/eae438287beda538a63d46f7a32aa31c9249e19d)
- [修改 Connection 处理消息的逻辑](https://github.com/fanyong920/jvppeteer/commit/6e53c7a8cd96648025369cbca451392416a84a7c)
- [将 NetworkEventManager 的 Map改为 HashMap](https://github.com/fanyong920/jvppeteer/commit/f21bc163e099404d2681780c5a683875847348c0)

### Version Release 2.1.2(2024/10/29)

- [下载浏览器添加 channel milestone buiid选项](https://github.com/fanyong920/jvppeteer/commit/477f0c616c4c851a36e72c8787ca5ae002d0b9c2)
- [给Browser加上AutoCloseable接口](https://github.com/fanyong920/jvppeteer/commit/ac535f860a1b1fe42eb5cb33b4b0974d77c3e3d9)

### Version Release 2.2.0(2024/11/05)

- [Browser 添加取消下载功能](https://github.com/fanyong920/jvppeteer/commit/4f46fd5099f4b5e7bbe1b1fa95865b7f0963ce7d)
- [从默认参数 中删除 --disable-component-update](https://github.com/fanyong920/jvppeteer/commit/73e673d2307008c259adefc16ca68282070abbcd)
- [将 chrome 首选版本升级为 130.0.6723.58](https://github.com/fanyong920/jvppeteer/commit/673a9588758769da149610678cb71e806d186a85)
- [添加下载文件的功能](https://github.com/fanyong920/jvppeteer/commit/3a59b4c3efcb1c584d465248d5a0db34fe004a65)

### Version Release 2.2.1(2024/11/12)

- [修复： ExceptionThrownEvent 中timestamp的类型](https://github.com/fanyong920/jvppeteer/commit/e669225b4fdc330b1e7a0b500664c7a1677a5aa5)

### Version Release 2.2.2(2024/11/18)

- [当设置--user-data-dir参数后，会报错Can't get WSEndpoint](https://github.com/fanyong920/jvppeteer/issues/164)
- [线程 JvEmitEventThread 和 JvHandleMessageThread CPU占用极高](https://github.com/fanyong920/jvppeteer/issues/163)

### Version Release 2.2.3(2024/11/18)

- [线程 JvEmitEventThread 和 JvHandleMessageThread CPU占用极高](https://github.com/fanyong920/jvppeteer/issues/163)

### Version Release 2.2.4(2024/11/21)

- 修改获取chrome pid 的方式：放弃通过 cdp 的 SystemInfo.getProcessInfo 方式

### Version Release 2.2.5(2024/11/22)

- [fix: correctly resolve OOPIF response bodies](https://github.com/fanyong920/jvppeteer/commit/ca19d06697567b9984f610b20a26a4a1c1cc1c79)
- [增强 Browser 关闭逻辑](https://github.com/fanyong920/jvppeteer/commit/d13eb41b11901da5986cbc8b8d1e288d802d8d5f)

### Version Release 3.0.0(2024/12/17)

- 添加对 firefox 浏览器的支持
- 添加 Webdriver-bidi 协议支持

### Version Release 3.1.0(2024/12/19)

- [fix:Page.waitForRequest 和 Page.waitForResponse 的等待逻辑修改](https://github.com/fanyong920/jvppeteer/commit/c75830dd711c8dc826672988103d575be6fb1fac)

- [fix:ElementHandle 的 waitForSelector中 selector 升级为 updatedSelector](https://github.com/fanyong920/jvppeteer/commit/43e2e7a87eaac5765d5bc290567e05c1e28ae677)

- [test:增加 DeviceRequestPrompt 测试](https://github.com/fanyong920/jvppeteer/commit/fd967063cc9ab75a05c9038ad3148a359de4d0c9)

- [test:增加Frame api的测试](https://github.com/fanyong920/jvppeteer/commit/7997f8dbfa612f2fb89cd7e5272721c17b024a96)

- [fix: Binding.run](https://github.com/fanyong920/jvppeteer/commit/f4dd1b5d68a805d73fc81376368e46b39c2ae012)

- [fix: Page.waitForSelector 查询 aria selector 的逻辑](https://github.com/fanyong920/jvppeteer/commit/4ace9f0bcca3fdd7a5213ae3fe8081067954afb8)

- [fix: Accessibility.snapshot 参数传入 root](https://github.com/fanyong920/jvppeteer/commit/ed315c3e0bb6272c9bb6e2e9b478c2b17ba51ae6)

- [Accessibility.snapshot 方法支持包括 iframe 在内](https://github.com/fanyong920/jvppeteer/commit/2c992d6df9d175fa9a419ade3a4a3c1e1b9cd811)

- [在 ElementHandle 添加 backendNodeId 方法](https://github.com/fanyong920/jvppeteer/commit/29d6d6c3cca8f5901a26160600a097f49f74afcc)

- [支持 reducedContrast 在 Page.emulateVisionDeficiency](https://github.com/fanyong920/jvppeteer/commit/0e9d7dcb0d681757006153dcf3398f37dee30d48)

- ### Version Release 3.1.1(2024/12/20)

  - [fix: firefox 默认协议是 Webdriver-bidi,chrome 默认协议是 CDP](https://github.com/fanyong920/jvppeteer/commit/444589d411d5169a295396c8f5a650802b0fa52b)
  - [fix: WaitTask 支持传 0 代表无限等待](https://github.com/fanyong920/jvppeteer/commit/00e023781dd729497b6fd4a19767012a89bf616d)
  - [fix: TimeoutSettings 构造方法初始化属性为 null](https://github.com/fanyong920/jvppeteer/commit/e13d15a826118ddb3b1ddf7fc1d0c0a03e555b0d)
  - [fix:TaskManager 中 task 属性改用 CopyOnWriteArraySet](https://github.com/fanyong920/jvppeteer/commit/025eed67768dee5ff328a9697f804bd28c6b69a3)
  - [TimeoutSettings 类的超时逻辑该写，支持 0 代表无限等待](https://github.com/fanyong920/jvppeteer/commit/0d36fa56d92eba7a7008f71eb4a537f4a29b61a1)
  - [Connection 等待时间改为 ProtocolTimeout](https://github.com/fanyong920/jvppeteer/commit/c6396b1f8af8994efc9131350c5d4e8292f626ec)
  - [简化 Callback 等待响应的逻辑](https://github.com/fanyong920/jvppeteer/commit/58018699e9c57cab25c69914093b13765fbbece6)

### Version Release 3.1.2(2024/12/26)

- [fix: Page.waitForFunction参数args传递修正](https://github.com/fanyong920/jvppeteer/commit/8aab6efda94839fa035a739e9035f311d9fac10b)
- [fix: 将 BrowserConnectOptions 的 protocolType 属性改为 protocol ,同时移除 LaunchOptions 的 protocol 属性](https://github.com/fanyong920/jvppeteer/commit/2f3dd31d23c0dde79b5961b20edfc1351a92b638)
- [fix: WebSocketTransportFactory 的 User-Agent 中的版本号 由 Constant.JVPPETEER_VERSION 代替](https://github.com/fanyong920/jvppeteer/commit/c44a177c0af201abfadb6d61af80f1ba7b9b9991)

### Version Release 3.1.3(2024/12/30)

- [fix: 启动浏览器判断文件名与产品是否符合](https://github.com/fanyong920/jvppeteer/commit/b4b5d995c743fa0c6e762f5c4c2fbc6ba1a00e5c)
- [fix: 将多线程下的变量加上 volatile](https://github.com/fanyong920/jvppeteer/commit/aca6fdfcaf99279b3d315fb636045b61f48f0688)
- [fix: 下载浏览器版本出错](https://github.com/fanyong920/jvppeteer/commit/a015b4e4fd5348efa581b969ab6f5a207c859252)
- [update: readme中jvppeteer版本加上斜杆](https://github.com/fanyong920/jvppeteer/commit/c5e56f48eecebd6bb289932c7e5f1c7a1c837d77)

### Version Release 3.1.4(2025/1/6)

- [fix: BrowserContext的cookies() 方法报错](https://github.com/fanyong920/jvppeteer/commit/63c268e3abdd400b1ae2826587f3624fbe56c39a)

### Version Release 3.1.5(2025/1/17)

- [fix:closeBrowser方法使用 process.waitFor](https://github.com/fanyong920/jvppeteer/commit/63a191cb118afc09e206e609dabe45b1fcd3462b)
- [fix:LaunchOptions 默认参数Product = CHROME](https://github.com/fanyong920/jvppeteer/commit/8c991d342089178a3bbab7642864870c34702c78)

### Version Release 3.1.6(2025/2/5)

- [update: PaperFormats添加以厘米为计算单位的注释](https://github.com/fanyong920/jvppeteer/commit/e00d414fbaf93e46e76899ea8a7c868f1af96127)
- [fix:launch()方法中校验逻辑全用小写](https://github.com/fanyong920/jvppeteer/commit/ff2524d356ebce1718c2c362d560a808a2f27a42)
- [chrome: use `goog:` prefix for BiDi+ commands](https://github.com/fanyong920/jvppeteer/commit/99d3a24873361ab4e751136015536e67395b1ed5)

### Version Release 3.1.7(2025/2/5)

- [fix: 删除 ExecutablePath 与 product一致的验证](https://github.com/fanyong920/jvppeteer/commit/5d792c2f0833820b1041199013497a9f2db1d2d7)

### Version Release 3.1.8(2025/2/6)

- fix:BrowserLauncher.getBrowserPid()方法添加cdp协议获取pid

### Version Release 3.2.0(2025/2/11)

- [fix:Frame的 hasStartedLoading 变量改成 public](https://github.com/fanyong920/jvppeteer/commit/5eeef4163bfeb90499083136c43c89f67a4f7603)
- [fix:FrameManager frameTreeHandled 变量添加 volatile 修饰符；格式化代码；createIsolatedWorld()函数中，key添加：分割以及this.client改成参数中的session，以及不等待返回结果](https://github.com/fanyong920/jvppeteer/commit/1c170d14508ee8b9d52a32ae368f91b6b2bbdb1e)
- [fix:CdpFrame的 loaderId 变量添加 volatile 修饰符](https://github.com/fanyong920/jvppeteer/commit/54351d89dd41b75c04a4d31b61e9a3c6c6a39700)
- [fix:LifecycleWatcher.checkLifecycle()参数Frame改为CdpFrame，以及格式化代码，frameDetached（）函数名改成onFrameDetached（）](https://github.com/fanyong920/jvppeteer/commit/e8ae3e762218d6d38c56bf0e92b89fbe411c5f86)

### Version Release 3.3.0(2025/2/20)

- [refactor: remove browser-specific code in cdp](https://github.com/fanyong920/jvppeteer/commit/2dfc67b792b97ebc29005545d4faa9cde04a8922)
- [refactor: remove support for Firefox over CDP](https://github.com/fanyong920/jvppeteer/commit/ec72c9f7a71d9cf97a92e2c9561d4724864f1b1c)
- [fix: dispose the isolate handle](https://github.com/fanyong920/jvppeteer/commit/36c02730222da75035d609963c80c9b784808661)
- [fix: BrowserContext的setCookies()的实现，参数改成CookieData](https://github.com/fanyong920/jvppeteer/commit/a2239caf3f0a2052594dca6be0ed121ff8b88928)
- [fix(webdriver): make sure user agent can be set twice](https://github.com/fanyong920/jvppeteer/commit/bc3cce6971826bf502fee47ce727530a5c469360)
- [feat: add keyboard-lock and pointer-lock permissions](https://github.com/fanyong920/jvppeteer/commit/96b3d3764c193d9673d9b59c36c64f670cbd55ee)
- [fix: IsolatedWorld的evaluateHandle() 和 evaluate() 添加方法内的注释](https://github.com/fanyong920/jvppeteer/commit/993a1593ecf7f470513b9c305cc7a534fc36c2eb)
- [feat: Browser类添加cookies()和setCookies()方法](https://github.com/fanyong920/jvppeteer/commit/85b25c9ee9cd8d40beb76ead1060ad9b8e3c8ed1)
- [fix: don't wait for activation if the connection is disconnected](https://github.com/fanyong920/jvppeteer/commit/29188aca9c27331cbabb048eb84de0ded09b7b81)
- [fix(network):propagate CDP error in interception](https://github.com/fanyong920/jvppeteer/commit/598729b9607356125ca57b9cea61859977689bd8)

### Version Release 3.3.1(2025/3/12)

- [fix: QueryHandlerUtil rename to GetQueryHandler](https://github.com/fanyong920/jvppeteer/commit/e31927c03e9b4950f87a97511ad2783395fbf6a2)
- [fix: 添加src属性来判断页面是否存在parsel-js](https://github.com/fanyong920/jvppeteer/commit/14529019eced0cdb0df0a33476617755d49309cf)
- [fix: ExecutionContext.initPuppeteerUtil()中返回值使用evaluateHandle()方法转化](https://github.com/fanyong920/jvppeteer/commit/be46916d8bf3387e86eb2105fb0cb308ba07052d)
- [fix: 添加更多的选择器支持以及测试样例](https://github.com/fanyong920/jvppeteer/commit/e0184d366499a01771c56f1068c50da95f70ab3b)
- [feat: expose CDPSession.detached()](https://github.com/fanyong920/jvppeteer/commit/730c3730feabe09383973270b13a6069e30797dc)
- [fix: 启动失败时候的错误日志中 chrome 改成 browser](https://github.com/fanyong920/jvppeteer/commit/a412ebb91d5033f4d2a5c3feb3005ec3eeb03ca8)

### Version Release 3.3.2(2025/3/12)

- [fix: parsel-js前加上#标识符](https://github.com/fanyong920/jvppeteer/commit/37c45ad6874b9fb00805001a9038622ee7e1d87a)

### Version Release 3.3.3(2025/3/24)

- [fix:Optimize waitForCondition to Reduce CPU Usage](https://github.com/fanyong920/jvppeteer/commit/9fa4165e40b4e1548f281c429fa766f3d5a6279c)

### Version Release 3.3.4(2025/3/26)

- [fix:parsel-js由src引入改为本地js代码](https://github.com/fanyong920/jvppeteer/commit/60d7b9daacb5d4d5ebca44cfc6de8bdbd62ee126)

### Version Release 3.3.5(2025/3/26)

- [fix:parsel-js由src引入改为本地js代码](https://github.com/fanyong920/jvppeteer/commit/60d7b9daacb5d4d5ebca44cfc6de8bdbd62ee126)