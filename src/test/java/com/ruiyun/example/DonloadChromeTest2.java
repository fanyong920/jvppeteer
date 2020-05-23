package com.ruiyun.example;

import com.ruiyun.jvppeteer.util.DownloadUtil;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DonloadChromeTest2 {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

        //https://npm.taobao.org/mirrors/chromium-browser-snapshots 更改为这个可以下载
        DownloadUtil.download("https://npm.taobao.org/mirrors/chromium-browser-snapshots/Win_x64/737027/chrome-win.zip","D:\\develop\\project\\toString\\cheom.zip",null);
//        DownLoadUtil.download("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1589985912478&di=317465b42a507946eae2e489d7e94ca8&imgtype=0&src=http%3A%2F%2Fa0.att.hudong.com%2F64%2F76%2F20300001349415131407760417677.jpg","E:\\2345Downloads\\asd.jpg");
    }
}
