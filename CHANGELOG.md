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