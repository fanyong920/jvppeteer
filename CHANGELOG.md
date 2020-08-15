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