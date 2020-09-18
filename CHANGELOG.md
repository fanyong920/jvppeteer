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